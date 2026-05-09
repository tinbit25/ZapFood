package com.example.food.data.model

import java.util.UUID

/**
 * OrderStatusHistory — Tracks a single state transition in the order lifecycle.
 */
data class OrderStatusHistory(
    val id: String = UUID.randomUUID().toString(),
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val actor: String = "SYSTEM", // "USER", "VENDOR", "ADMIN", "SYSTEM"
    val actorName: String = "",
    val notes: String = ""
)
