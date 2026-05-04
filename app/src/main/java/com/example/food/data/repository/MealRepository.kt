package com.example.food.data.repository

import com.example.food.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class MealRepository {

    private val masterChefId = java.util.UUID.randomUUID().toString()

    private val mockMeals = listOf(
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Butter Chicken",
            description = "Rich and creamy butter chicken with basmati rice.",
            imageUrl = "https://images.unsplash.com/photo-1603894527026-267daa770047?w=500&auto=format&fit=crop",
            calories = 650,
            price = 12.0,
            vendorId = masterChefId,
            vendorName = "Master Chef"
        ),
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Chicken Briyani",
            description = "Fragrant rice with spiced chicken.",
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?w=500&auto=format&fit=crop",
            calories = 550,
            price = 14.0,
            vendorId = masterChefId,
            vendorName = "Master Chef"
        ),
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Spaghetti Bolognese",
            description = "Classic Italian pasta with beef sauce.",
            imageUrl = "https://images.unsplash.com/photo-1622973536968-3ead9e780960?w=500&auto=format&fit=crop",
            calories = 500,
            price = 15.0,
            vendorId = masterChefId,
            vendorName = "Master Chef"
        )
    )

    private val mockMealPlans = listOf(
        MealPlan(
            id = java.util.UUID.randomUUID().toString(),
            name = "Bachelors Safe Haven",
            description = "Perfect plan for busy individuals.",
            imageUrl = "https://images.unsplash.com/photo-1543332164-6e82f355bab7?w=800&auto=format&fit=crop",
            type = MealPlanType.MONTHLY,
            price = 350.0,
            vendorId = masterChefId,
            vendorName = "Master Chef",
            meals = mockMeals,
            nutritionalSummary = NutritionalSummary(1500, 200f, 150f, 80f),
            mpcode = "KR-BACH-99"
        ),
        MealPlan(
            id = java.util.UUID.randomUUID().toString(),
            name = "Maseba's Table",
            description = "Traditional local delicacies.",
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800&auto=format&fit=crop",
            type = MealPlanType.WEEKLY,
            price = 85.0,
            vendorId = java.util.UUID.randomUUID().toString(),
            vendorName = "Abiye Briggs",
            meals = mockMeals.take(2),
            nutritionalSummary = NutritionalSummary(1800, 180f, 140f, 70f),
            mpcode = "KR-MASE-01"
        )
    )

    fun getMeals(): Flow<List<Meal>> = flowOf(mockMeals)
    
    fun getMealPlans(): Flow<List<MealPlan>> = flowOf(mockMealPlans)
    
    fun getMealPlanById(id: String): MealPlan? {
        return mockMealPlans.find { it.id == id }
    }
}
