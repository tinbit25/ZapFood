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
    val name: String,
    val imageUrl: String,
    val price: Double,
    val vendorId: String,
    val matchScore: Double,
    val reason: String,
    val tags: List<String>
)

data class AIRecommendationResponse(
    val recommendedMeals: List<ScoredMealResponse>,
    val generatedAt: String,
    val processingTimeMs: Double
)

data class AIAnalyticsEventRequest(
    val userId: String,
    val eventType: String,
    val mealId: String? = null,
    val recommendationContext: String? = null
)
