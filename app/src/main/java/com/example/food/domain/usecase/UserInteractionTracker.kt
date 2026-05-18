package com.example.food.domain.usecase

import android.content.Context
import com.example.food.data.repository.InteractionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserInteractionTracker(
    context: Context,
    private val repository: InteractionRepository = InteractionRepository()
) {
    private val sharedPrefs = context.getSharedPreferences("user_interactions_learning", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun trackClick(userId: String, mealId: String, category: String, vendorId: String) {
        scope.launch {
            repository.trackMealClick(userId, mealId)
        }
        // Increment local counts for real-time client-side learning
        incrementCount("meal_click_$mealId")
        incrementCount("category_click_$category")
        incrementCount("vendor_click_$vendorId")
    }

    fun trackFavorite(userId: String, mealId: String, isFavorite: Boolean) {
        scope.launch {
            repository.trackFavorite(userId, mealId, isFavorite)
        }
        if (isFavorite) {
            incrementCount("meal_fav_$mealId")
        } else {
            decrementCount("meal_fav_$mealId")
        }
    }

    fun getMealInteractionScore(mealId: String, category: String, vendorId: String): Double {
        val clickCount = getCount("meal_click_$mealId")
        val favCount = getCount("meal_fav_$mealId")
        val categoryClick = getCount("category_click_$category")
        val vendorClick = getCount("vendor_click_$vendorId")

        return (clickCount * 1.5) + (favCount * 5.0) + (categoryClick * 0.5) + (vendorClick * 1.0)
    }

    private fun incrementCount(key: String) {
        val current = sharedPrefs.getInt(key, 0)
        sharedPrefs.edit().putInt(key, current + 1).apply()
    }

    private fun decrementCount(key: String) {
        val current = sharedPrefs.getInt(key, 0)
        if (current > 0) {
            sharedPrefs.edit().putInt(key, current - 1).apply()
        }
    }

    private fun getCount(key: String): Int {
        return sharedPrefs.getInt(key, 0)
    }
}
