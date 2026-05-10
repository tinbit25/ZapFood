package com.example.food.domain.model

import com.example.food.data.model.Meal
import com.example.food.data.model.UserFoodPreference

data class ScoredMeal(
    val meal: Meal,
    val final_score: Float,
    val score_breakdown: Map<String, Float>,
    val cultural_reasoning: String
)

data class RecommendationResponse(
    val recommendations: List<ScoredMeal>,
    val processing_time_ms: Float
)

data class RecommendationRequest(
    val user_preference: UserFoodPreference,
    val candidate_meals: List<Meal>,
    val current_time_of_day: String? = null,
    val current_day_of_week: String? = null
)

data class SimilarityRequest(
    val target_meal_id: String,
    val candidate_meals: List<Meal>,
    val top_n: Int = 5
)

data class CartContext(
    val cart_meals: List<Meal>,
    val time_of_day: String = "Lunch",
    val day_of_week: String = "Monday",
    val weather: String = "Clear",
    val user_id: String? = null
)

data class ComboRequest(
    val context: CartContext,
    val candidate_meals: List<Meal>,
    val top_n: Int = 3
)

data class ComboRecommendation(
    val meal: Meal,
    val match_score: Float,
    val upsell_reason: String
)

// Legacy Placeholders (can be removed or kept for reference)
data class MealEmbedding(
    val mealId: String,
    val vector: FloatArray
)
