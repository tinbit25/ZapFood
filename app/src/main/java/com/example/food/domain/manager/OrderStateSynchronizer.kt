package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.example.food.data.model.PaymentStatus
import com.example.food.data.repository.OrderRepository

/**
 * OrderStateSynchronizer
 *
 * Enforces business rules that span both [OrderStatus] and [PaymentStatus].
 * When payment is confirmed, the order moves to ACCEPTED automatically.
 * When an order is cancelled, the payment is marked as REFUNDED.
 *
 * All mutations are delegated to [OrderRepository] to ensure Firestore is
 * the single source of truth.
 */
class OrderStateSynchronizer(
    private val orderRepository: OrderRepository
) {

    /**
     * Called after a payment update. If payment succeeded, advance order
     * status to ACCEPTED (if it is still PENDING).
     */
    suspend fun onPaymentStatusChanged(
        orderId: String,
        newPaymentStatus: PaymentStatus,
        currentOrder: Order
    ): Resource<Unit> {
        return when (newPaymentStatus) {
            PaymentStatus.SUCCESS -> {
                if (currentOrder.orderStatus == OrderStatus.PENDING) {
                    val targetStatus = if (currentOrder.orderType == com.example.food.data.model.OrderType.DINE_IN) {
                        OrderStatus.BOOKED 
                    } else {
                        OrderStatus.ACCEPTED
                    }
                    
                    orderRepository.updateOrderStatus(
                        orderId = orderId,
                        status = targetStatus,
                        actor = "SYSTEM",
                        notes = "Auto-advanced to $targetStatus after payment confirmation."
                    )
                } else {
                    Resource.Success(Unit) // Already progressed, nothing to do.
                }
            }
            PaymentStatus.FAILED -> {
                // Cancel the order if payment fails and it hasn't been accepted yet.
                if (currentOrder.orderStatus == OrderStatus.PENDING) {
                    orderRepository.updateOrderStatus(
                        orderId = orderId,
                        status = OrderStatus.CANCELLED,
                        actor = "SYSTEM",
                        notes = "Auto-cancelled due to payment failure."
                    )
                } else {
                    Resource.Success(Unit)
                }
            }
            else -> Resource.Success(Unit)
        }
    }

    /**
     * Called after an order status update. If the order is CANCELLED,
     * mark the payment as REFUNDED.
     */
    suspend fun onOrderStatusChanged(
        orderId: String,
        newOrderStatus: OrderStatus
    ): Resource<Unit> {
        return if (newOrderStatus == OrderStatus.CANCELLED) {
            orderRepository.updatePaymentStatus(orderId, PaymentStatus.REFUNDED)
        } else {
            Resource.Success(Unit)
        }
    }

    /**
     * Validates that an order is in a legal state (not missing critical
     * status combinations).
     */
    fun isConsistent(order: Order): Boolean {
        // A delivered order must have a successful payment.
        if (order.orderStatus == OrderStatus.DELIVERED &&
            order.paymentStatus != PaymentStatus.SUCCESS) return false
        // A pending order should not have a refunded payment.
        if (order.orderStatus == OrderStatus.PENDING &&
            order.paymentStatus == PaymentStatus.REFUNDED) return false
        return true
    }
}
