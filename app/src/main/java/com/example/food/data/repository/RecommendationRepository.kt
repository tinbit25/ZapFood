package com.example.food.data.repository

import com.example.food.data.api.AiRecommendationApi
import com.example.food.data.model.Meal
import com.example.food.data.model.UserFoodPreference
import com.example.food.domain.model.CartContext
import com.example.food.domain.model.ComboRecommendation
import com.example.food.domain.model.ComboRequest
import com.example.food.domain.model.RecommendationRequest
import com.example.food.domain.model.ScoredMeal
import com.example.food.domain.model.SimilarityRequest
import com.example.food.core.util.Resource
import com.example.food.data.model.MealFilters
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class RecommendationRepository(
    private val api: AiRecommendationApi = AiRecommendationApi(),
    private val mealRepository: MealRepository = MealRepository(),
    private val userPreferenceRepository: UserPreferenceRepository = UserPreferenceRepository()
) {
    // In-memory cache to avoid spamming the backend
    private var cachedPersonalized: List<ScoredMeal>? = null
    private var cachedTrending: List<ScoredMeal>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes

    suspend fun getPersonalizedRecommendations(userId: String): Result<List<ScoredMeal>> {
        if (cachedPersonalized != null && isCacheValid()) {
            return Result.success(cachedPersonalized!!)
        }

        val prefResult = userPreferenceRepository.getPreferences(userId)
        val pref = prefResult.data ?: UserFoodPreference(userId = userId)

        // Fetch all meals as candidates
        val mealsResult = mealRepository.getFilteredMeals(MealFilters()).firstOrNull { it is Resource.Success } as? Resource.Success<List<Meal>>
        if (mealsResult != null) {
            val req = RecommendationRequest(
                user_preference = pref,
                candidate_meals = mealsResult.data!!,
                current_day_of_week = getDayOfWeek()
            )
            val result = api.getPersonalized(req)
            if (result.isSuccess) {
                cachedPersonalized = result.getOrNull()?.recommendations
                cacheTimestamp = System.currentTimeMillis()
                return Result.success(cachedPersonalized!!)
            }
            return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
        return Result.failure(Exception("Could not fetch candidate meals"))
    }

    suspend fun getTrendingMeals(userId: String): Result<List<ScoredMeal>> {
        if (cachedTrending != null && isCacheValid()) {
            return Result.success(cachedTrending!!)
        }

        val prefResult = userPreferenceRepository.getPreferences(userId)
        val pref = prefResult.data ?: UserFoodPreference(userId = userId)

        val mealsResult = mealRepository.getFilteredMeals(MealFilters()).firstOrNull { it is Resource.Success } as? Resource.Success<List<Meal>>
        if (mealsResult != null) {
            val req = RecommendationRequest(
                user_preference = pref,
                candidate_meals = mealsResult.data!!
            )
            val result = api.getTrending(req)
            if (result.isSuccess) {
                cachedTrending = result.getOrNull()?.recommendations
                return Result.success(cachedTrending!!)
            }
            return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
        return Result.failure(Exception("Could not fetch candidate meals"))
    }

    suspend fun getFastingPicks(userId: String): Result<List<ScoredMeal>> {
        val prefResult = userPreferenceRepository.getPreferences(userId)
        val pref = prefResult.data ?: UserFoodPreference(userId = userId)

        val mealsResult = mealRepository.getFilteredMeals(MealFilters()).firstOrNull { it is Resource.Success } as? Resource.Success<List<Meal>>
        if (mealsResult != null) {
            val req = RecommendationRequest(
                user_preference = pref,
                candidate_meals = mealsResult.data!!
            )
            val result = api.getFasting(req)
            if (result.isSuccess) {
                return Result.success(result.getOrNull()?.recommendations ?: emptyList())
            }
            return Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
        return Result.failure(Exception("Could not fetch candidate meals"))
    }

    suspend fun getCartSuggestions(userId: String, cartMeals: List<Meal>): Result<List<ComboRecommendation>> {
        if (cartMeals.isEmpty()) return Result.success(emptyList())

        val mealsResult = mealRepository.getFilteredMeals(MealFilters()).firstOrNull { it is Resource.Success } as? Resource.Success<List<Meal>>
        if (mealsResult != null) {
            val context = CartContext(
                cart_meals = cartMeals,
                day_of_week = getDayOfWeek(),
                user_id = userId
                // weather could be fetched from a weather service here
            )
            val req = ComboRequest(context = context, candidate_meals = mealsResult.data!!)
            return api.getCartSuggestions(req)
        }
        return Result.failure(Exception("Could not fetch candidate meals"))
    }

    private fun isCacheValid(): Boolean {
        return (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION_MS
    }

    private fun getDayOfWeek(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }
    }
}
