package com.example.food.data.model

import java.util.UUID

enum class Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

enum class PlanSourceType {
    VENDOR, CUSTOMER, AI
}


data class MealPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val vendorId: String = "", // For vendor-created plans
    val businessName: String = "",
    val sourceType: PlanSourceType = PlanSourceType.CUSTOMER,
    val meals: Map<Day, List<String>> = emptyMap(), // Map of Day to List of Meal IDs
    val price: Double = 0.0,
    val mpcode: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
