package com.example.food.data.repository

import com.example.food.data.api.AiRecommendationApi
import com.example.food.domain.model.AIPersonalizedRequest
import com.example.food.domain.model.AIAnalyticsEventRequest
import com.example.food.domain.model.ScoredMealResponse
import java.util.Calendar

class RecommendationRepository(
    private val api: AiRecommendationApi = AiRecommendationApi()
) {
    // In-memory cache
    private var cachedPersonalized: List<ScoredMealResponse>? = null
    private var cachedTrending: List<ScoredMealResponse>? = null
    private var cachedFasting: List<ScoredMealResponse>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes

    suspend fun getPersonalizedRecommendations(userId: String): Result<List<ScoredMealResponse>> {
        if (cachedPersonalized != null && isCacheValid()) {
            return Result.success(cachedPersonalized!!)
        }

        val req = AIPersonalizedRequest(
            userId = userId,
            mealTime = getTimeOfDay(),
            currentDay = getDayOfWeek()
        )
        
        val result = api.getPersonalized(req)
        if (result.isSuccess) {
            cachedPersonalized = result.getOrNull()?.recommendedMeals
            cacheTimestamp = System.currentTimeMillis()
            return Result.success(cachedPersonalized!!)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    suspend fun getTrendingMeals(userId: String): Result<List<ScoredMealResponse>> {
        if (cachedTrending != null && isCacheValid()) {
            return Result.success(cachedTrending!!)
        }

        val result = api.getTrending()
        if (result.isSuccess) {
            cachedTrending = result.getOrNull()?.recommendedMeals
            return Result.success(cachedTrending!!)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    suspend fun getFastingPicks(userId: String): Result<List<ScoredMealResponse>> {
        if (cachedFasting != null && isCacheValid()) {
            return Result.success(cachedFasting!!)
        }

        val result = api.getFasting()
        if (result.isSuccess) {
            cachedFasting = result.getOrNull()?.recommendedMeals
            return Result.success(cachedFasting!!)
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }
    
    suspend fun getBreakfastIdeas(userId: String): Result<List<ScoredMealResponse>> {
        val result = api.getBreakfast()
        if (result.isSuccess) {
            return Result.success(result.getOrNull()?.recommendedMeals ?: emptyList())
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    suspend fun getSimilarMeals(userId: String, mealId: String): Result<List<ScoredMealResponse>> {
        val result = api.getSimilarMeals(mealId)
        if (result.isSuccess) {
            return Result.success(result.getOrNull()?.recommendedMeals ?: emptyList())
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }

    suspend fun getCartSuggestions(userId: String, cartMealIds: List<String>): Result<List<ScoredMealResponse>> {
        // Just fetching combo for the last added meal for simplicity
        if (cartMealIds.isEmpty()) return Result.success(emptyList())
        
        val targetMealId = cartMealIds.last()
        val result = api.getCombos(targetMealId)
        if (result.isSuccess) {
            return Result.success(result.getOrNull()?.recommendedMeals ?: emptyList())
        }
        return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
    }
    
    suspend fun trackAnalyticsEvent(
        userId: String, 
        eventType: String, 
        mealId: String? = null, 
        context: String? = null
    ) {
        val request = AIAnalyticsEventRequest(
            userId = userId,
            eventType = eventType,
            mealId = mealId,
            recommendationContext = context
        )
        api.trackEvent(request)
    }

    private fun isCacheValid(): Boolean {
        return (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }

    private fun getDayOfWeek(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> "Monday"
        }
    }

    private fun getTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..10 -> "Morning"
            hour in 11..15 -> "Lunch"
            hour in 16..22 -> "Dinner"
            else -> "Late Night"
        }
    }
}
