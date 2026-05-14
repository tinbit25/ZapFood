package com.example.food.data.model

import java.util.UUID

enum class TicketCategory {
    PAYMENT, DELIVERY, TECHNICAL, GENERAL
}

enum class TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED
}

data class SupportTicket(
    val ticketId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val relatedOrderId: String? = null,
    val category: TicketCategory = TicketCategory.GENERAL,
    val message: String = "",
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TicketResponse(
    val responseId: String = UUID.randomUUID().toString(),
    val ticketId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Feedback(
    val feedbackId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val orderId: String? = null,
    val vendorId: String? = null,
    val vendorName: String? = null,
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
