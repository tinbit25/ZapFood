package com.example.food.core.util

import com.example.food.data.model.Meal
import com.example.food.data.model.NutritionalSummary

object NutritionCalculator {
    fun calculateSummary(meals: List<Meal>): NutritionalSummary {
        return NutritionalSummary(
            totalCalories = meals.sumOf { it.calories },
            totalProtein = meals.sumOf { it.protein.toDouble() }.toFloat(),
            totalCarbs = meals.sumOf { it.carbs.toDouble() }.toFloat(),
            totalFats = meals.sumOf { it.fats.toDouble() }.toFloat()
        )
    }

    fun combineSummaries(summaries: List<NutritionalSummary>): NutritionalSummary {
        return NutritionalSummary(
            totalCalories = summaries.sumOf { it.totalCalories },
            totalProtein = summaries.sumOf { it.totalProtein.toDouble() }.toFloat(),
            totalCarbs = summaries.sumOf { it.totalCarbs.toDouble() }.toFloat(),
            totalFats = summaries.sumOf { it.totalFats.toDouble() }.toFloat()
        )
    }
}
