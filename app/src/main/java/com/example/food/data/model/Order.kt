package com.example.food.data.model

import java.util.UUID

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

// Enums are now managed in Payment.kt to maintain a single source of truth for the payment domain

data class OrderItem(
    val mealId: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1
)

data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val customerId: String = "",
    val customerName: String = "",
    val vendorId: String = "",
    val vendorName: String = "",
    val mealPlanId: String? = null,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.INITIATED,
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val deliveryTrackingId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
