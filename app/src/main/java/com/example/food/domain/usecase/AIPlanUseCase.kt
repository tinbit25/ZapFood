package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.remote.AIService
import com.example.food.data.remote.MockAIService
import com.example.food.data.repository.MealRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

class AIPlanUseCase(
    private val aiService: AIService = MockAIService(),
    private val mealRepository: MealRepository = MealRepository()
) {
    suspend fun generateMealPlan(preferences: AIPreference): Resource<MealPlan> {
        return try {
            // 1. Get AI suggestions
            val aiOutput = aiService.generateMealPlan(preferences)
            
            // 2. Batch fetch all available meals for mapping
            val allMealsResource = mealRepository.getFilteredMeals(MealFilters()).first()
            val availableMeals = (allMealsResource as? Resource.Success)?.data ?: emptyList()
            
            if (availableMeals.isEmpty()) {
                return Resource.Error("No meals available in the system. Please add meals first.")
            }

            // 3. Map suggestions to real Meal entities
            val mappedPlan = mutableMapOf<Day, List<String>>()
            var currentTotalCalories = 0

            for ((day, suggestions) in aiOutput.suggestions) {
                val dayMealIds = suggestions.map { suggestion ->
                    val bestMatch = findBestMatch(suggestion, availableMeals, preferences.dietaryPreferences)
                    currentTotalCalories += bestMatch.calories
                    bestMatch.id
                }
                mappedPlan[day] = dayMealIds
            }

            // 4. Create the MealPlan object
            val mealPlan = MealPlan(
                id = UUID.randomUUID().toString(),
                name = "AI ${preferences.goal.name.lowercase().replaceFirstChar { it.uppercase() }} Plan",
                description = "Personalized plan generated for your ${preferences.goal.name} goal.",
                ownerId = preferences.userId,
                imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800",
                price = calculateBasePrice(mappedPlan, availableMeals),
                nutritionalSummary = NutritionalSummary(totalCalories = currentTotalCalories),
                meals = mappedPlan,
                sourceType = PlanSourceType.AI,
                isPublic = false
            )

            Resource.Success(mealPlan)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "AI Generation failed")
        }
    }

    private fun findBestMatch(suggestion: MealSuggestion, meals: List<Meal>, dietaryPrefs: List<String>): Meal {
        // Priority 1: Exact name match
        val nameMatch = meals.find { it.name.equals(suggestion.name, ignoreCase = true) }
        if (nameMatch != null && isSafe(nameMatch, dietaryPrefs)) return nameMatch

        // Priority 2: Category + Closest Calories
        val categoryMeals = meals.filter { it.category.equals(suggestion.category, ignoreCase = true) && isSafe(it, dietaryPrefs) }
        if (categoryMeals.isNotEmpty()) {
            return categoryMeals.minByOrNull { kotlin.math.abs(it.calories - suggestion.targetCalories) }!!
        }

        // Priority 3: Any safe meal closest to target calories
        return meals.filter { isSafe(it, dietaryPrefs) }
            .minByOrNull { kotlin.math.abs(it.calories - suggestion.targetCalories) }
            ?: meals.first() // Fallback to any meal if nothing fits (should not happen with good data)
    }

    private fun isSafe(meal: Meal, dietaryPrefs: List<String>): Boolean {
        // Simple check: if dietaryPrefs contains 'vegan', meal must have 'vegan' tag
        // This can be expanded based on the Meal entity's tags
        return dietaryPrefs.all { pref -> 
            meal.name.contains(pref, ignoreCase = true) // Placeholder logic
        }
    }

    private fun calculateBasePrice(plan: Map<Day, List<String>>, meals: List<Meal>): Double {
        var total = 0.0
        val mealMap = meals.associateBy { it.id }
        plan.values.flatten().forEach { id ->
            total += mealMap[id]?.price ?: 0.0
        }
        return total
    }
}
