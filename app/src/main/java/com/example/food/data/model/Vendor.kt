package com.example.food.data.model

data class Vendor(
    val vendorId: String = "",
    val vendorName: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val cuisineType: String = "",
    val rating: Float = 0f,
    val location: String = "",
    val menu: List<Meal> = emptyList()
)
