package com.example.food.data.repository

import com.example.food.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MealRepository {

    private val mockMeals = listOf(
        Meal(
            mealId = "m1",
            mealName = "Butter Chicken",
            description = "Rich and creamy butter chicken with basmati rice.",
            imageUrl = "https://images.unsplash.com/photo-1603894527026-267daa770047?w=500&auto=format&fit=crop",
            calories = 650,
            price = 12.0,
            vendorName = "Master Chef"
        ),
        Meal(
            mealId = "m2",
            mealName = "Chicken Briyani",
            description = "Fragrant rice with spiced chicken.",
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?w=500&auto=format&fit=crop",
            calories = 550,
            price = 14.0,
            vendorName = "Master Chef"
        ),
        Meal(
            mealId = "m3",
            mealName = "Spaghetti Bolognese",
            description = "Classic Italian pasta with beef sauce.",
            imageUrl = "https://images.unsplash.com/photo-1622973536968-3ead9e780960?w=500&auto=format&fit=crop",
            calories = 500,
            price = 15.0,
            vendorName = "Master Chef"
        )
    )

    private val mockMealPlans = listOf(
        MealPlan(
            mealPlanId = "mp1",
            mealPlanName = "Bachelors Safe Haven",
            description = "Perfect plan for busy individuals.",
            imageUrl = "https://images.unsplash.com/photo-1543332164-6e82f355bab7?w=800&auto=format&fit=crop",
            type = MealPlanType.MONTHLY,
            price = 350.0,
            vendorName = "Master Chef",
            meals = mockMeals,
            nutritionalSummary = NutritionalSummary(1500, 200f, 150f, 80f),
            mpcode = "KR-BACH-99"
        ),
        MealPlan(
            mealPlanId = "mp2",
            mealPlanName = "Maseba's Table",
            description = "Traditional local delicacies.",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800&auto=format&fit=crop",
            type = MealPlanType.WEEKLY,
            price = 85.0,
            vendorName = "Abiye Briggs",
            meals = mockMeals.take(2),
            nutritionalSummary = NutritionalSummary(1800, 180f, 140f, 70f),
            mpcode = "KR-MASE-01"
        )
    )

    fun getMeals(): Flow<List<Meal>> = flowOf(mockMeals)
    
    fun getMealPlans(): Flow<List<MealPlan>> = flowOf(mockMealPlans)
    
    fun getMealPlanById(id: String): MealPlan? {
        return mockMealPlans.find { it.mealPlanId == id }
    }
}
