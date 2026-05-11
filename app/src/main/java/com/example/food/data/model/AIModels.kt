package com.example.food.data.model

data class ScoredRecommendation(
    val mealId: String,
    val mealName: String,
    val score: Int,
    val reason: String
)

data class SmartRecommendationResponse(
    val smartPicks: List<ScoredRecommendation>,
    val fastingMeals: List<ScoredRecommendation>,
    val popularInAddis: List<ScoredRecommendation>,
    val reasoning: String,
    val nutritionSummary: String
)
