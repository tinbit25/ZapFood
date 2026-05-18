package com.example.food.domain.usecase

import com.example.food.data.model.*
import java.util.Calendar

data class RecommendationResult(
    val meal: Meal,
    val finalScore: Double,
    val explanation: String
)

class ContextAwareRecommendationEngine(
    private val interactionTracker: UserInteractionTracker
) {
    fun scoreAndRankMeals(
        meals: List<Meal>,
        preferences: UserFoodPreference,
        isFastingDay: Boolean = EthiopianBehaviorIntelligence.isFastingDay()
    ): List<RecommendationResult> {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return meals.map { meal ->
            var score = 10.0 // Base score
            val explanations = mutableListOf<String>()

            // 1. Fasting Boosting
            if (isFastingDay) {
                if (meal.fastingFriendly || meal.veganFriendly || meal.isFastingMeal() || meal.isVeganMeal()) {
                    score += 15.0
                    explanations.add("Strict Fasting Day Pick 🌱")
                } else if (meal.isMeatMeal()) {
                    score -= 12.0
                }
            } else {
                // If user prefers fasting-friendly by default
                if (preferences.fastingMode && (meal.fastingFriendly || meal.isFastingMeal())) {
                    score += 5.0
                    explanations.add("Matches Fasting Lifestyle")
                }
            }

            // 2. Time-of-Day Contextual Boosting
            val mealTimeRelevance = getMealTimeRelevance(meal, hour)
            if (mealTimeRelevance > 0) {
                score += mealTimeRelevance * 5.0
                explanations.add(getTimeOfDayLabel(hour))
            }

            // 3. User taste spice matching
            if (meal.spiceLevel == preferences.spicePreference) {
                score += 8.0
                explanations.add("Suits your ${preferences.spicePreference.name.lowercase()} spice taste 🌶️")
            }

            // 4. Budget Preference matching
            val budgetMatch = isBudgetMatch(meal, preferences.budgetPreference)
            if (budgetMatch) {
                score += 6.0
                explanations.add("Fits your budget preference 💰")
            }

            // 5. User dynamic interactions
            val interactionScore = interactionTracker.getMealInteractionScore(meal.id, meal.category, meal.vendorId)
            if (interactionScore > 0) {
                score += interactionScore
                explanations.add("Highly relevant based on your interactions ✨")
            }

            // 6. Food Category Match
            if (preferences.favoriteCategories.contains(meal.category)) {
                score += 4.0
                explanations.add("From your favorite category")
            }

            // Fallback default explanations if empty
            if (explanations.isEmpty()) {
                if (meal.popularityScore > 0.7) {
                    explanations.add("Trending in Addis Ababa 🔥")
                } else {
                    explanations.add("Popular near you 📍")
                }
            }

            // Final explanation construction
            val explanation = explanations.firstOrNull() ?: "Recommended for you"
            
            RecommendationResult(
                meal = meal,
                finalScore = score,
                explanation = explanation
            )
        }.sortedByDescending { it.finalScore }
    }

    private fun getMealTimeRelevance(meal: Meal, hour: Int): Int {
        return when {
            // Breakfast time: 5 AM to 10:59 AM
            hour in 5..10 -> {
                if (meal.mealTime.contains(MealTime.BREAKFAST) || meal.category.contains("breakfast", ignoreCase = true) || meal.name.contains("chechebsa", ignoreCase = true) || meal.name.contains("coffee", ignoreCase = true)) 3 else 0
            }
            // Lunch time: 11 AM to 3:59 PM
            hour in 11..15 -> {
                if (meal.mealTime.contains(MealTime.LUNCH) || meal.name.contains("tibs", ignoreCase = true) || meal.name.contains("pasta", ignoreCase = true) || meal.name.contains("rice", ignoreCase = true)) 3 else 0
            }
            // Dinner time: 4 PM to 9:59 PM
            hour in 16..21 -> {
                if (meal.mealTime.contains(MealTime.DINNER) || meal.name.contains("family", ignoreCase = true) || meal.name.contains("pizza", ignoreCase = true) || meal.name.contains("wat", ignoreCase = true)) 3 else 0
            }
            // Late Night: 10 PM to 4:59 AM
            else -> {
                if (meal.mealTime.contains(MealTime.SNACK) || meal.name.contains("shawarma", ignoreCase = true) || meal.name.contains("burger", ignoreCase = true)) 3 else 0
            }
        }
    }

    private fun getTimeOfDayLabel(hour: Int): String {
        return when (hour) {
            in 5..10 -> "Fresh Morning Pick ☀️"
            in 11..15 -> "Perfect Lunch Idea 🍽️"
            in 16..21 -> "Top Dinner Choice 🌃"
            else -> "Late-Night Snack Cravings 🌙"
        }
    }

    private fun isBudgetMatch(meal: Meal, budgetPref: String): Boolean {
        return when (budgetPref) {
            "BUDGET" -> meal.price < 150.0
            "STANDARD" -> meal.price in 150.0..400.0
            "PREMIUM" -> meal.price > 400.0
            else -> true
        }
    }
}
