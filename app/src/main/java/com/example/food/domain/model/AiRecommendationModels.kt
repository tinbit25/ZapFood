package com.example.food.domain.model

data class AIPersonalizedRequest(
    val userId: String,
    val mealTime: String = "lunch",
    val fastingMode: Boolean = false,
    val currentDay: String = "Monday",
    val weather: String = "Sunny"
)

data class AICartComboRequest(
    val userId: String,
    val cartMealIds: List<String>
)

data class ScoredMealResponse(
    val mealId: String,
    val mealName: String,
    val score: Double,
    val reason: String
)

data class AIRecommendationResponse(
    val recommendations: List<ScoredMealResponse>,
    val reasoning: String,
    val nutritionSummary: String
)

data class AIAnalyticsEventRequest(
    val userId: String,
    val eventType: String,
    val mealId: String? = null,
    val recommendationContext: String? = null
)
