package com.example.food.data.model

import java.util.UUID

data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fats: Float = 0f,
    val price: Double = 0.0,
    val vendorId: String = "",
    val vendorName: String = "",
    val category: String = "General",
    val isAvailable: Boolean = true,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

data class MealFilters(
    val category: String? = null,
    val minCalories: Int? = null,
    val maxCalories: Int? = null,
    val vendorId: String? = null,
    val query: String? = null
)
