package com.example.food.domain.model

data class ScoredMealResponse(
    val mealId: String,
    val mealName: String,
    val score: Double,
    val reason: String
)

data class AIRecommendationResponse(
    val smartPicks: List<ScoredMealResponse> = emptyList(),
    val fastingMeals: List<ScoredMealResponse> = emptyList(),
    val popularInAddis: List<ScoredMealResponse> = emptyList(),
    val reasoning: String = "",
    val nutritionSummary: String = ""
)

data class AIAnalyticsEventRequest(
    val userId: String,
    val eventType: String,
    val mealId: String? = null,
    val recommendationContext: String? = null
)
