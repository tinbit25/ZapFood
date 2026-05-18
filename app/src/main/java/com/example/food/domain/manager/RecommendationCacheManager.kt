package com.example.food.domain.manager

import android.content.Context
import com.example.food.data.model.Meal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecommendationCacheManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("recommendation_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun cacheMeals(key: String, meals: List<Meal>) {
        val json = gson.toJson(meals)
        sharedPreferences.edit().putString(key, json).apply()
    }

    fun getCachedMeals(key: String): List<Meal>? {
        val json = sharedPreferences.getString(key, null) ?: return null
        return try {
            val type = object : TypeToken<List<Meal>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        sharedPreferences.edit().clear().apply()
    }
}
