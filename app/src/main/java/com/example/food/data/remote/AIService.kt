package com.example.food.data.remote

import com.example.food.data.model.*
import kotlinx.coroutines.delay

interface AIService {
    suspend fun generateMealPlan(preferences: AIPreference): AIGeneratedPlan
}

class MockAIService : AIService {
    override suspend fun generateMealPlan(preferences: AIPreference): AIGeneratedPlan {
        delay(2000) // Simulate network/AI delay
        
        val suggestions = Day.entries.associateWith { day ->
            (1..preferences.mealsPerDay).map { index ->
                MealSuggestion(
                    name = when(index) {
                        1 -> "Healthy Breakfast Bowl"
                        2 -> "Grilled Chicken Salad"
                        else -> "Grilled Salmon with Veggies"
                    },
                    category = when(index) {
                        1 -> "Breakfast"
                        2 -> "Lunch"
                        else -> "Dinner"
                    },
                    targetCalories = preferences.calorieTarget / preferences.mealsPerDay,
                    tags = preferences.dietaryPreferences
                )
            }
        }
        
        return AIGeneratedPlan(
            suggestions = suggestions,
            totalCalories = preferences.calorieTarget,
            alignedWithGoal = true
        )
    }
}
