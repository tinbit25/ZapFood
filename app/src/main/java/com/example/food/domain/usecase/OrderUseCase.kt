package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.OrderRepository
import com.example.food.data.repository.MealRepository
import com.example.food.data.remote.NotificationService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OrderUseCase(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val mealRepository: MealRepository = MealRepository(),
    private val paymentUseCase: PaymentUseCase = PaymentUseCase(),
    private val notificationService: NotificationService = NotificationService(),
    private val userPreferenceUseCase: UserPreferenceUseCase = UserPreferenceUseCase(),
    private val analyticsPreparationUseCase: AnalyticsPreparationUseCase = AnalyticsPreparationUseCase(),
    private val qrVerificationService: com.example.food.domain.manager.QRVerificationService = com.example.food.domain.manager.QRVerificationService(orderRepository)
) {
    /**
     * Customer places an order for a list of meals.
     */
    suspend fun placeOrder(user: User, mealIds: List<String>, mealPlanId: String? = null): Resource<Order> {
        if (user.role != UserRole.CUSTOMER) {
            return Resource.Error("Only customers can place orders")
        }

        if (mealIds.isEmpty()) return Resource.Error("Cannot place an empty order")

        // Group IDs to handle quantities
        val idCounts = mealIds.groupingBy { it }.eachCount()
        val orderItems = mutableListOf<OrderItem>()
        var subtotal = 0.0
        var vendorId: String? = null
        var businessName: String? = null

        for ((id, quantity) in idCounts) {
            val meal = mealRepository.getMealById(id) ?: return Resource.Error("Meal not found: $id")
            if (!meal.isAvailable) return Resource.Error("Meal unavailable: ${meal.name}")

            // Enforce single-vendor per order for simplicity
            if (vendorId == null) {
                vendorId = meal.vendorId
                businessName = meal.businessName
            } else if (vendorId != meal.vendorId) {
                return Resource.Error("Orders must contain meals from a single vendor")
            }

            orderItems.add(OrderItem(meal.id, meal.name, meal.price, quantity, meal.category, meal.fastingFriendly))
            subtotal += (meal.price * quantity)
        }

        val deliveryFee = 2.0 // Unit: thousands of ETB (e.g. 2.0 = 2000 ETB)
        val order = Order(
            orderId = UUID.randomUUID().toString(),
            customerId = user.userId,
            customerName = user.displayName ?: "Unknown",
            vendorId = vendorId ?: "",
            businessName = businessName ?: "",
            mealPlanId = mealPlanId,
            items = orderItems,
            totalAmount = subtotal + deliveryFee,
            deliveryFee = deliveryFee,
            orderStatus = OrderStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = orderRepository.saveOrder(order)
        if (result is Resource.Success) {
            // Fire and forget behavior profile update
            CoroutineScope(Dispatchers.IO).launch {
                userPreferenceUseCase.updateProfileAfterOrder(order)
                analyticsPreparationUseCase.processOrderAnalytics(order)
            }
            return Resource.Success(order)
        }
        return Resource.Error(result.message ?: "Failed to place order")
    }

    /**
     * Strict State Machine Transition Check
     */
    private fun canTransition(current: OrderStatus, next: OrderStatus): Boolean {
        return when (current) {
            OrderStatus.INITIATED -> next == OrderStatus.PAYMENT_PENDING || next == OrderStatus.CANCELLED || next == OrderStatus.PENDING || next == OrderStatus.SENT_TO_VENDOR
            OrderStatus.PAYMENT_PENDING -> next == OrderStatus.PAYMENT_PROCESSING || next == OrderStatus.CANCELLED || next == OrderStatus.PAID
            OrderStatus.PAYMENT_PROCESSING -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED
            OrderStatus.PAID -> next == OrderStatus.SENT_TO_VENDOR || next == OrderStatus.CANCELLED
            OrderStatus.SENT_TO_VENDOR -> next == OrderStatus.ACCEPTED || next == OrderStatus.CANCELLED

            OrderStatus.PENDING, OrderStatus.BOOKED -> next == OrderStatus.ACCEPTED || next == OrderStatus.CANCELLED || next == OrderStatus.ARRIVED
            OrderStatus.ACCEPTED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED
            OrderStatus.PREPARING -> next == OrderStatus.READY
            OrderStatus.READY -> next == OrderStatus.ON_THE_WAY || next == OrderStatus.DELIVERED
            OrderStatus.ARRIVED -> next == OrderStatus.READY || next == OrderStatus.DELIVERED
            OrderStatus.ON_THE_WAY -> next == OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> false // Terminal state
            OrderStatus.CANCELLED -> false // Terminal state
        }
    }

    suspend fun updateOrderStatus(user: User, orderId: String, nextStatus: OrderStatus): Resource<Unit> {
        val order = orderRepository.getOrderById(orderId) ?: return Resource.Error("Order not found")

        // 1. Authorization checks
        when (user.role) {
            UserRole.VENDOR -> {
                if (order.vendorId != user.userId) return Resource.Error("Unauthorized: You do not own this order")
            }
            UserRole.CUSTOMER -> {
                if (order.customerId != user.userId) return Resource.Error("Unauthorized: This is not your order")
                // Customers can only cancel
                if (nextStatus != OrderStatus.CANCELLED) return Resource.Error("Unauthorized: Customers can only cancel orders")
            }
            UserRole.ADMIN -> {} // Admins can do anything
        }

        // 2. Business Logic & Payment checks
        if (nextStatus == OrderStatus.ACCEPTED) {
            // Vendors cannot accept unpaid digital orders (for Cash on Delivery, it stays INITIATED)
            if (order.paymentMethod != PaymentMethod.CASH && order.paymentStatus != PaymentStatus.SUCCESS) {
                return Resource.Error("Order cannot be accepted until payment is successful")
            }
        }

        if (nextStatus == OrderStatus.CANCELLED) {
            if (user.role == UserRole.CUSTOMER && order.orderStatus != OrderStatus.PENDING) {
                return Resource.Error("Cannot cancel order once it is accepted by vendor")
            }
            
            // Handle Refunds
            if (order.paymentStatus == PaymentStatus.SUCCESS) {
                paymentUseCase.refundPayment(orderId)
            }
        }

        // 3. State Machine enforcement
        if (!canTransition(order.orderStatus, nextStatus)) {
            return Resource.Error("Invalid transition from ${order.orderStatus} to $nextStatus")
        }

        val result = orderRepository.updateOrderStatus(
            orderId = orderId,
            status = nextStatus,
            actor = user.role.name,
            actorName = user.displayName ?: user.userId
        )

        if (result is Resource.Success) {
            val notificationType = when (nextStatus) {
                OrderStatus.ACCEPTED -> NotificationType.ORDER_ACCEPTED
                OrderStatus.PREPARING -> NotificationType.MEAL_PREPARING
                OrderStatus.READY -> NotificationType.ORDER_READY
                OrderStatus.ARRIVED -> NotificationType.ORDER_STATUS_UPDATE // Or specific one
                OrderStatus.ON_THE_WAY -> NotificationType.DELIVERY_ON_THE_WAY
                OrderStatus.DELIVERED -> NotificationType.ORDER_DELIVERED
                OrderStatus.CANCELLED -> NotificationType.ORDER_CANCELLED
                else -> NotificationType.ORDER_STATUS_UPDATE
            }
            
            // Notify the customer about the status update
            notificationService.notifyOrderUpdate(
                userId = order.customerId,
                orderId = orderId,
                type = notificationType
            )
        }

        return result
    }

    fun getMyOrders(userId: String): Flow<Resource<List<Order>>> {
        return orderRepository.getOrdersForUser(userId)
    }

    fun getIncomingOrders(vendorId: String): Flow<Resource<List<Order>>> {
        return orderRepository.getOrdersForVendor(vendorId)
    }

    suspend fun verifyPickup(orderId: String, token: String): Resource<Unit> {
        val order = orderRepository.getOrderById(orderId) ?: return Resource.Error("Order not found")
        return qrVerificationService.verifyToken(order, token)
    }
}
