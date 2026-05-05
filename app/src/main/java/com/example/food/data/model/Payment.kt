package com.example.food.data.model

import java.util.UUID

enum class PaymentStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    FAILED,
    REFUNDED
}

enum class PaymentMethod {
    CARD,
    MOBILE_MONEY,
    CASH
}

data class Payment(
    val paymentId: String = UUID.randomUUID().toString(),
    val orderId: String = "",
    val userId: String = "",
    val amount: Long = 0, // In smallest currency unit (e.g., RWF)
    val method: PaymentMethod = PaymentMethod.CARD,
    val status: PaymentStatus = PaymentStatus.INITIATED,
    val transactionRef: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
