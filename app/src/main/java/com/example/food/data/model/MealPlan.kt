package com.example.food.data.model

import java.util.UUID

enum class Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum class PlanSourceType {
    VENDOR, CUSTOMER
}

data class NutritionalSummary(
    val totalCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFats: Float = 0f
)

data class MealPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val vendorId: String = "", // For vendor-created plans
    val vendorName: String = "",
    val sourceType: PlanSourceType = PlanSourceType.CUSTOMER,
    val meals: Map<Day, List<String>> = emptyMap(), // Map of Day to List of Meal IDs
    val nutritionalSummary: NutritionalSummary = NutritionalSummary(),
    val price: Double = 0.0,
    val mpcode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
