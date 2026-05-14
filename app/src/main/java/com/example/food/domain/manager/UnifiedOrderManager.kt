package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.example.food.data.model.OrderType
import com.example.food.data.model.PaymentStatus
import com.example.food.data.repository.OrderRepository
import com.example.food.domain.util.OrderTypeResolver

/**
 * UnifiedOrderManager
 *
 * Single entry-point for all order lifecycle operations across DELIVERY,
 * TAKEAWAY, and DINE_IN. Composes [OrderRepository], [OrderTypeResolver],
 * and [OrderStateSynchronizer] to guarantee:
 *
 *  1. Type-specific validation before saving.
 *  2. Atomic status transitions via Firestore transactions.
 *  3. Automatic cross-field synchronization (payment ↔ order status).
 */
class UnifiedOrderManager(
    private val orderRepository: OrderRepository,
    private val synchronizer: OrderStateSynchronizer
) {
    private val qrPickupManager = QRPickupManager(orderRepository)

    // ── Order Creation ────────────────────────────────────────────────────────

    /**
     * Validates and persists a new [Order] for any order type.
     * Returns [Resource.Error] if type-specific validation fails.
     */
    suspend fun placeOrder(order: Order): Resource<Order> {
        // 1. Validate type-specific info
        val validation = OrderTypeResolver.validate(order)
        if (validation is OrderTypeResolver.ValidationResult.Invalid) {
            return Resource.Error(validation.reason)
        }

        // 2. Validate item list
        if (order.items.isEmpty()) {
            return Resource.Error("Order must contain at least one item.")
        }

        // 3. Persist
        return when (val result = orderRepository.saveOrder(order)) {
            is Resource.Success -> Resource.Success(order)
            is Resource.Error   -> Resource.Error(result.message ?: "Failed to save order.")
            is Resource.Loading -> Resource.Loading()
        }
    }

    // ── Status Transitions ────────────────────────────────────────────────────

    /**
     * Transitions [orderId] to a new [OrderStatus] and synchronizes any
     * downstream state (e.g., refund on cancellation).
     */
    suspend fun transitionOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        actor: String = "SYSTEM",
        actorName: String = "",
        notes: String = ""
    ): Resource<Unit> {
        // 1. Persist the status change
        val updateResult = orderRepository.updateOrderStatus(
            orderId   = orderId,
            status    = newStatus,
            actor     = actor,
            actorName = actorName,
            notes     = notes
        )
        if (updateResult is Resource.Error) return updateResult

        // 2. Sync downstream effects (e.g., mark payment REFUNDED on CANCEL)
        return synchronizer.onOrderStatusChanged(orderId, newStatus)
    }

    /**
     * Updates [PaymentStatus] for [orderId] and advances [OrderStatus]
     * automatically when payment succeeds.
     */
    suspend fun syncPaymentStatus(
        orderId: String,
        newPaymentStatus: PaymentStatus
    ): Resource<Unit> {
        // 1. Persist payment status
        val paymentResult = orderRepository.updatePaymentStatus(orderId, newPaymentStatus)
        if (paymentResult is Resource.Error) return paymentResult

        // 2. Load current order to evaluate cross-field sync
        val order = orderRepository.getOrderById(orderId)
            ?: return Resource.Error("Order not found: $orderId")

        // 3. Synchronize order status based on new payment status
        val syncResult = synchronizer.onPaymentStatusChanged(orderId, newPaymentStatus, order)
        
        // 4. Generate QR Code if it's a Takeaway order and payment succeeded
        if (newPaymentStatus == PaymentStatus.SUCCESS && order.orderType == OrderType.TAKEAWAY) {
            qrPickupManager.assignQRToOrder(order)
        }
        
        return syncResult
    }

    // ── Retrieval ─────────────────────────────────────────────────────────────

    /**
     * Fetches a single order by ID. Returns null if not found.
     */
    suspend fun getOrder(orderId: String): Order? = orderRepository.getOrderById(orderId)

    /**
     * Returns a snapshot check on whether the given order's state fields
     * are internally consistent.
     */
    suspend fun isOrderConsistent(orderId: String): Boolean {
        val order = orderRepository.getOrderById(orderId) ?: return false
        return synchronizer.isConsistent(order)
    }
}
