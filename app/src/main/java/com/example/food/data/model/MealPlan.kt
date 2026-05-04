package com.example.food.data.model

import java.util.UUID

enum class MealPlanType {
    WEEKLY, MONTHLY
}

data class NutritionalSummary(
    val totalCalories: Int,
    val protein: Float,
    val carbs: Float,
    val fats: Float
)

data class MealPlan(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val imageUrl: String,
    val type: MealPlanType,
    val price: Double,
    val vendorId: UUID,
    val vendorName: String,
    val meals: List<Meal> = emptyList(),
    val nutritionalSummary: NutritionalSummary,
    val mpcode: String = "",
    val ownerId: String = "" // Keep String for Firebase ID or update to UUID if stored locally
)
