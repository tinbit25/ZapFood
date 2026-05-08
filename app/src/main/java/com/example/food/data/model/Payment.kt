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
    val amount: Long = 0, // In smallest currency unit (santim for ETB)
    val currency: String = "ETB",
    val method: PaymentMethod = PaymentMethod.CARD,
    val status: PaymentStatus = PaymentStatus.INITIATED,
    val transactionRef: String? = null,
    val chapaRef: String? = null, // Chapa's internal reference ID
    val checkoutUrl: String? = null, // Chapa hosted checkout page URL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
