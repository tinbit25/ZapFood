package com.example.food.data.model

enum class HealthGoal {
    WEIGHT_LOSS, MUSCLE_GAIN, MAINTENANCE
}

enum class DurationType {
    WEEKLY, MONTHLY
}

data class AIPreference(
    val userId: String,
    val dietaryPreferences: List<String>,
    val allergies: List<String>,
    val calorieTarget: Int,
    val goal: HealthGoal,
    val mealsPerDay: Int,
    val duration: DurationType
)

data class MealSuggestion(
    val name: String,
    val category: String,
    val targetCalories: Int,
    val tags: List<String> = emptyList()
)

data class AIGeneratedPlan(
    val suggestions: Map<Day, List<MealSuggestion>>,
    val totalCalories: Int,
    val alignedWithGoal: Boolean
)
