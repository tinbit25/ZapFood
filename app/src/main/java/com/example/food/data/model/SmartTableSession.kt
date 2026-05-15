package com.example.food.data.model

import java.util.UUID

/**
 * SmartTableSession
 * Represents an active dine-in session bound to a specific table.
 */
/**
 * SmartTableSession
 * Represents an active dine-in session bound to a specific table.
 */
data class SmartTableSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val vendorId: String = "",
    val tableId: String = "",
    val tableNumber: String = "",
    val branchId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val orders: List<String> = emptyList() // List of Order IDs in this session
)
