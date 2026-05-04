package com.example.food.data.model

enum class MealPlanType {
    WEEKLY, MONTHLY
}

data class MealPlan(
    val mealPlanId: String = "",
    val mealPlanName: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val type: MealPlanType = MealPlanType.WEEKLY,
    val ownerId: String = "", // Vendor or User ID
    val mpcode: String = "",
    val meals: List<Meal> = emptyList(),
    val price: Double = 0.0,
    val vendorName: String = "",
    val nutritionalSummary: NutritionalSummary = NutritionalSummary()
)

data class NutritionalSummary(
    val totalCalories: Int = 0,
    val totalCarbs: Float = 0f,
    val totalProteins: Float = 0f,
    val totalFats: Float = 0f
)
