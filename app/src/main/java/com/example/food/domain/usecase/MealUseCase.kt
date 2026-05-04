package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.example.food.data.model.User
import com.example.food.data.model.UserRole
import com.example.food.data.repository.MealRepository
import kotlinx.coroutines.flow.Flow

class MealUseCase(
    private val mealRepository: MealRepository = MealRepository()
) {
    suspend fun createMeal(user: User, meal: Meal): Resource<Unit> {
        // 1. Check user role = VENDOR
        if (user.role != UserRole.VENDOR && user.role != UserRole.ADMIN) {
            return Resource.Error("Unauthorized: Only vendors can create meals")
        }

        // 2. Validate input
        if (meal.name.isBlank()) return Resource.Error("Meal name cannot be empty")
        if (meal.calories < 0 || meal.protein < 0 || meal.carbs < 0 || meal.fats < 0) {
            return Resource.Error("Invalid nutrition values: Macros cannot be negative")
        }
        if (meal.price <= 0) return Resource.Error("Price must be greater than zero")

        // 3. Attach vendorId automatically
        val mealToSave = meal.copy(
            vendorId = user.userId,
            vendorName = user.displayName ?: "Unknown Vendor"
        )

        // 4. Save meal
        return mealRepository.saveMeal(mealToSave)
    }

    fun getFilteredMeals(filters: MealFilters): Flow<Resource<List<Meal>>> {
        return mealRepository.getFilteredMeals(filters)
    }

    suspend fun getMealDetails(id: String): Meal? {
        return mealRepository.getMealById(id)
    }
}
