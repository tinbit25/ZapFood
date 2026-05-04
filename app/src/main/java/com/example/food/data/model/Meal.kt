package com.example.food.data.model

data class Meal(
    val mealId: String = "",
    val mealName: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val calories: Int = 0,
    val carbs: Float = 0f,
    val proteins: Float = 0f,
    val fats: Float = 0f,
    val ingredients: List<String> = emptyList(),
    val vendorId: String = "",
    val vendorName: String = "",
    val price: Double = 0.0
)
