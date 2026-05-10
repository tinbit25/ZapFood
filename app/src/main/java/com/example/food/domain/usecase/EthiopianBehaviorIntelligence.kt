package com.example.food.domain.usecase

import com.example.food.data.model.Order
import com.example.food.data.model.UserFoodPreference
import java.util.Calendar

object EthiopianBehaviorIntelligence {

    fun isFastingDay(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Ethiopian fasting is strictly on Wednesday (4) and Friday (6)
        return dayOfWeek == Calendar.WEDNESDAY || dayOfWeek == Calendar.FRIDAY
    }

    fun calculateUpdatedPreferences(
        currentPrefs: UserFoodPreference,
        newOrder: Order
    ): UserFoodPreference {
        
        val newMealCounts = currentPrefs.mealOrderCounts.toMutableMap()
        val newVendorCounts = currentPrefs.vendorInteractionCounts.toMutableMap()
        
        // Update vendor count
        val vId = newOrder.vendorId
        if (vId.isNotEmpty()) {
            newVendorCounts[vId] = (newVendorCounts[vId] ?: 0) + 1
        }

        // Update meal counts
        newOrder.items.forEach { item ->
            newMealCounts[item.mealId] = (newMealCounts[item.mealId] ?: 0) + item.quantity
        }

        // Calculate favorite vendors (top 3)
        val favoriteVendors = newVendorCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        // Calculate favorite meals (top 5)
        val learnedFavoriteMeals = newMealCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        // Calculate frequently ordered categories
        val categoryCounts = mutableMapOf<String, Int>()
        newOrder.items.forEach { item ->
            categoryCounts[item.category] = (categoryCounts[item.category] ?: 0) + item.quantity
        }
        
        // Add existing frequency logic if needed, but for simplicity we will just 
        // append or rely on a more complex historical analysis later.
        val freqCategories = currentPrefs.frequentlyOrderedCategories.toMutableSet()
        freqCategories.addAll(categoryCounts.keys)

        return currentPrefs.copy(
            favoriteMeals = learnedFavoriteMeals,
            favoriteVendors = favoriteVendors,
            frequentlyOrderedCategories = freqCategories.toList(),
            vendorInteractionCounts = newVendorCounts,
            mealOrderCounts = newMealCounts,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
