package com.example.food.data.model

import java.util.UUID

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}

enum class DeliveryStatus {
    IDLE,
    PICKED_UP,
    NEARBY,
    DELIVERED
}

// Enums are now managed in Payment.kt to maintain a single source of truth for the payment domain

enum class OrderType {
    DELIVERY,
    TAKEAWAY,
    DINE_IN
}

data class OrderItem(
    val mealId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val category: String = "GENERAL",
    val fastingFriendly: Boolean = false
)

data class DeliveryDetails(
    val address: String = "",
    val city: String = "Addis Ababa",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val instructions: String? = null,
    val estimatedArrival: Long? = null
)

data class TakeawayDetails(
    val pickupBranch: String = "",
    val pickupTime: Long? = null,
    val readyNotificationSent: Boolean = false
)

data class DineInDetails(
    val arrivalTime: Long? = null,
    val tableNumber: String? = null,
    val guestCount: Int = 1,
    val preorderReadyTime: Long? = null,
    val reservationConfirmed: Boolean = false
)

data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val customerId: String = "",
    val customerName: String = "",
    val vendorId: String = "",
    val businessName: String = "",
    val mealPlanId: String? = null,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.INITIATED,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val orderType: OrderType = OrderType.DELIVERY,
    
    // Type-specific details
    val deliveryDetails: DeliveryDetails? = null,
    val takeawayDetails: TakeawayDetails? = null,
    val dineInDetails: DineInDetails? = null,
    
    val deliveryTrackingId: String = "",
    val deliveryStatus: DeliveryStatus = DeliveryStatus.IDLE,
    val statusHistory: List<OrderStatusHistory> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
