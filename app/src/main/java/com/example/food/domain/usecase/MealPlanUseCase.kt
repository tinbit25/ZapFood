package com.example.food.domain.usecase

import com.example.food.core.util.NutritionCalculator
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.MealPlanRepository
import com.example.food.data.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MealPlanUseCase(
    private val mealPlanRepository: MealPlanRepository = MealPlanRepository(),
    private val mealRepository: MealRepository = MealRepository()
) {
    suspend fun getMealPlanById(id: String): Resource<MealPlan> {
        val plan = mealPlanRepository.getMealPlanById(id)
        return if (plan != null) Resource.Success(plan) else Resource.Error("Meal plan not found")
    }

    suspend fun createMealPlan(user: User, plan: MealPlan): Resource<Unit> {
        // 1. Validate rules
        if (plan.name.isBlank()) return Resource.Error("Meal plan name required")
        
        val totalMealsCount = plan.meals.values.sumOf { it.size }
        if (totalMealsCount == 0) return Resource.Error("Meal plan must have at least 1 meal")

        // 2. Validate all meals exist and are available
        for (mealIds in plan.meals.values) {
            for (mealId in mealIds) {
                val meal = mealRepository.getMealById(mealId)
                if (meal == null) return Resource.Error("Meal not found: $mealId")
                if (!meal.isAvailable) return Resource.Error("Meal currently unavailable: ${meal.name}")
            }
        }

        // 3. Attach owner and source
        val planToSave = plan.copy(
            ownerId = user.userId,
            sourceType = if (user.role == UserRole.VENDOR) PlanSourceType.VENDOR else PlanSourceType.CUSTOMER,
            vendorId = if (user.role == UserRole.VENDOR) user.userId else "",
            vendorName = if (user.role == UserRole.VENDOR) (user.displayName ?: "") else "",
            updatedAt = System.currentTimeMillis()
        )

        // 4. Recalculate nutrition before saving
        val finalPlan = recalculateNutrition(planToSave)

        return mealPlanRepository.saveMealPlan(finalPlan)
    }

    suspend fun recalculateNutrition(plan: MealPlan): MealPlan {
        val allMealIds = plan.meals.values.flatten()
        val meals = allMealIds.mapNotNull { mealRepository.getMealById(it) }
        
        return plan.copy(
            nutritionalSummary = NutritionCalculator.calculateSummary(meals)
        )
    }

    suspend fun addMealToDay(plan: MealPlan, day: Day, mealId: String): Resource<MealPlan> {
        val meal = mealRepository.getMealById(mealId) ?: return Resource.Error("Meal not found")
        if (!meal.isAvailable) return Resource.Error("Meal unavailable")

        val currentDayMeals = plan.meals[day]?.toMutableList() ?: mutableListOf()
        currentDayMeals.add(mealId)

        val updatedMeals = plan.meals.toMutableMap()
        updatedMeals[day] = currentDayMeals

        val updatedPlan = plan.copy(meals = updatedMeals)
        return Resource.Success(recalculateNutrition(updatedPlan))
    }

    suspend fun removeMealFromDay(plan: MealPlan, day: Day, mealId: String): Resource<MealPlan> {
        val currentDayMeals = plan.meals[day]?.toMutableList() ?: return Resource.Error("Day not found")
        currentDayMeals.remove(mealId)

        val updatedMeals = plan.meals.toMutableMap()
        if (currentDayMeals.isEmpty()) {
            updatedMeals.remove(day)
        } else {
            updatedMeals[day] = currentDayMeals
        }

        if (updatedMeals.isEmpty()) return Resource.Error("Cannot leave plan empty")

        val updatedPlan = plan.copy(meals = updatedMeals)
        return Resource.Success(recalculateNutrition(updatedPlan))
    }

    suspend fun cloneMealPlan(user: User, originalPlan: MealPlan): Resource<MealPlan> {
        val clonedPlan = originalPlan.copy(
            id = UUID.randomUUID().toString(),
            ownerId = user.userId,
            sourceType = PlanSourceType.CUSTOMER,
            name = "Copy of ${originalPlan.name}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mpcode = "" // Fresh start for sharing
        )
        // Note: No need to recalculate nutrition as it's a direct copy of meals
        val result = mealPlanRepository.saveMealPlan(clonedPlan)
        return if (result is Resource.Success) Resource.Success(clonedPlan) else Resource.Error(result.message ?: "Cloning failed")
    }

    fun getMyPlans(userId: String): Flow<Resource<List<MealPlan>>> {
        return mealPlanRepository.getMealPlansForUser(userId)
    }

    fun getDiscoverPlans(): Flow<Resource<List<MealPlan>>> {
        return mealPlanRepository.getVendorPlans()
    }

    suspend fun seedPlans(vendorIds: List<String>, mealIds: List<String>): Resource<Unit> {
        return mealPlanRepository.seedPlans(vendorIds, mealIds)
    }
}
