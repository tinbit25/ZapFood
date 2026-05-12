package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.example.food.data.model.User
import com.example.food.data.model.UserRole
import com.example.food.data.repository.MealRepository
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.Flow

class MealUseCase(
    private val mealRepository: MealRepository = MealRepository(),
    private val vendorRepository: VendorRepository = VendorRepository()
) {
    suspend fun createMeal(user: User, meal: Meal): Resource<Unit> {
        // 1. Check user role = VENDOR and status = APPROVED
        if (user.role == UserRole.VENDOR && user.vendorStatus != com.example.food.data.model.VendorStatus.APPROVED) {
            return Resource.Error("Your account is not approved to create meals yet. Status: ${user.vendorStatus}")
        }
        if (user.role != UserRole.VENDOR && user.role != UserRole.ADMIN) {
            return Resource.Error("Unauthorized: Only vendors can create meals")
        }

        // 2. Validate input
        if (meal.name.isBlank()) return Resource.Error("Meal name cannot be empty")
        if (meal.price <= 0) return Resource.Error("Price must be greater than zero")

        // 3. Fetch Vendor for Business Name
        val vendor = vendorRepository.getVendorByUserId(user.userId)
        val businessName = vendor?.businessName ?: user.displayName ?: "Unknown Vendor"

        // 4. Attach vendorId automatically
        val mealToSave = meal.copy(
            vendorId = user.userId,
            vendorName = businessName
        )

        // 5. Save meal
        return mealRepository.saveMeal(mealToSave)
    }

    fun getFilteredMeals(filters: MealFilters): Flow<Resource<List<Meal>>> {
        return mealRepository.getFilteredMeals(filters)
    }

    suspend fun getMealDetails(id: String): Meal? {
        return mealRepository.getMealById(id)
    }

    suspend fun seedMeals(vendorIds: List<String>): Resource<Unit> {
        return mealRepository.seedMeals(vendorIds)
    }

    suspend fun seedMealsForVendor(user: User): Resource<Unit> {
        val vendor = vendorRepository.getVendorByUserId(user.userId)
        val businessName = vendor?.businessName ?: user.displayName ?: "Unknown Vendor"
        return mealRepository.seedMealsForVendor(user.userId, businessName)
    }
}
