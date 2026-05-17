package com.example.food.data.model

import java.util.UUID

enum class OrderStatus {
    // Payment-Gated digital flows states
    INITIATED,
    PAYMENT_PENDING,
    PAYMENT_PROCESSING,
    PAID,
    SENT_TO_VENDOR,

    // Standard flow states
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED,
    
    // Arrive & Eat States
    BOOKED,
    ARRIVED
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
    val arrivalTime: Long? = null, // Legacy, will use expectedArrivalTime for new flow
    val expectedArrivalTime: String = "", // e.g. "12:30 PM"
    val tableNumber: String = "Pending",
    val guestCount: Int = 1,
    val preorderReadyTime: Long? = null,
    val reservationConfirmed: Boolean = false,
    val isCheckedIn: Boolean = false
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
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.INITIATED,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val orderType: OrderType = OrderType.DELIVERY,
    
    // Unified Info Fields
    val deliveryInfo: DeliveryDetails? = null,
    val pickupInfo: TakeawayDetails? = null,
    val dineInInfo: DineInDetails? = null,
    
    val deliveryTrackingId: String = "",
    val deliveryStatus: DeliveryStatus = DeliveryStatus.IDLE,
    val statusHistory: List<OrderStatusHistory> = emptyList(),
    
    // QR Takeaway Pickup Flow Fields
    val pickupQRCode: String = "",
    val pickupToken: String = "",
    val pickupVerified: Boolean = false,
    val pickupTimestamp: Long = 0L,
    val qrExpiresAt: Long = 0L,
    val tableClosed: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
