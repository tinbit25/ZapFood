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
        
        // Simple update logic for favorite foods and categories
        val newFavoriteFoods = currentPrefs.favoriteFoods.toMutableSet()
        newOrder.items.forEach { item ->
            newFavoriteFoods.add(item.mealId)
        }

        val newFavoriteVendors = currentPrefs.favoriteVendors.toMutableSet()
        if (newOrder.vendorId.isNotEmpty()) {
            newFavoriteVendors.add(newOrder.vendorId)
        }

        val newFavoriteCategories = currentPrefs.favoriteCategories.toMutableSet()
        newOrder.items.forEach { item ->
            newFavoriteCategories.add(item.category)
        }

        return currentPrefs.copy(
            favoriteFoods = newFavoriteFoods.toList().take(10),
            favoriteVendors = newFavoriteVendors.toList().take(5),
            favoriteCategories = newFavoriteCategories.toList().take(5),
            lastUpdated = System.currentTimeMillis()
        )
    }
}
