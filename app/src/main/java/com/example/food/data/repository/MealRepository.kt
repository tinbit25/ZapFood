package com.example.food.data.repository

import com.example.food.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MealRepository {

    private val mockMeals = listOf(
        Meal(
            mealId = "m1",
            mealName = "Quinoa Salad",
            description = "Healthy quinoa with vegetables",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop",
            calories = 350,
            price = 12.0,
            vendorName = "Green Leaf Cafe"
        ),
        Meal(
            mealId = "m2",
            mealName = "Grilled Salmon",
            description = "Atlantic salmon with asparagus",
            imageUrl = "https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=500&auto=format&fit=crop",
            calories = 450,
            price = 18.0,
            vendorName = "Ocean Delights"
        ),
        Meal(
            mealId = "m3",
            mealName = "Vegan Buddha Bowl",
            description = "A mix of healthy veggies and tofu",
            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop",
            calories = 400,
            price = 14.0,
            vendorName = "The Vegan Spot"
        )
    )

    private val mockMealPlans = listOf(
        MealPlan(
            mealPlanId = "mp1",
            mealPlanName = "Weight Loss Weekly",
            description = "A 7-day plan focused on low calorie intake.",
            imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500&auto=format&fit=crop",
            type = MealPlanType.WEEKLY,
            price = 85.0,
            vendorName = "NutriFit",
            meals = mockMeals,
            nutritionalSummary = NutritionalSummary(2100, 150f, 120f, 60f)
        ),
        MealPlan(
            mealPlanId = "mp2",
            mealPlanName = "Muscle Gain Monthly",
            description = "High protein plan for athletes.",
            imageUrl = "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=500&auto=format&fit=crop",
            type = MealPlanType.MONTHLY,
            price = 320.0,
            vendorName = "PowerEats",
            meals = mockMeals,
            nutritionalSummary = NutritionalSummary(3500, 300f, 250f, 100f)
        )
    )

    fun getMeals(): Flow<List<Meal>> = flowOf(mockMeals)
    
    fun getMealPlans(): Flow<List<MealPlan>> = flowOf(mockMealPlans)
    
    fun getMealPlanById(id: String): MealPlan? {
        return mockMealPlans.find { it.mealPlanId == id }
    }
}
