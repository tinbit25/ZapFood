package com.example.food.data.model

import java.util.Date

enum class OrderStatus {
    PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val mealPlanId: String? = null,
    val mealId: String? = null, // For single meal orders
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryAddress: String = ""
)

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED, REFUNDED
}

data class Payment(
    val paymentId: String = "",
    val orderId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)
