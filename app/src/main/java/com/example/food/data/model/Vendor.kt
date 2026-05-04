package com.example.food.data.model

import java.util.UUID

data class Vendor(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID, // Reference to User.id
    val businessName: String,
    val description: String,
    val logoUrl: String? = null,
    val rating: Float = 0f,
    val totalOrders: Int = 0,
    val isVerified: Boolean = false
)
