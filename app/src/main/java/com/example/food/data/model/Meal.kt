package com.example.food.data.model

import java.util.UUID

data class Meal(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val imageUrl: String,
    val calories: Int,
    val price: Double,
    val vendorId: UUID,
    val vendorName: String
)
