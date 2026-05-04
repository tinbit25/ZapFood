package com.example.food.data.model

import java.util.UUID

data class Meal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val imageUrl: String,
    val calories: Int,
    val price: Double,
    val vendorId: String,
    val vendorName: String
)
