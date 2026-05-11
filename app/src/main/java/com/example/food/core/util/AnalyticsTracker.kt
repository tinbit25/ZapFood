package com.example.food.core.util

import com.example.food.data.model.AnalyticsEvent
import com.example.food.data.model.InteractionType
import com.example.food.data.model.Meal
import com.example.food.data.repository.InteractionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

object AnalyticsTracker {
    private val repository = InteractionRepository()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessionId = UUID.randomUUID().toString()

    fun logEvent(userId: String, interactionType: InteractionType, meal: Meal? = null, metadata: Map<String, String> = emptyMap()) {
        val event = AnalyticsEvent(
            userId = userId,
            mealId = meal?.id,
            vendorId = meal?.vendorId,
            interactionType = interactionType,
            sessionId = sessionId,
            mealCategory = meal?.category,
            mealTags = meal?.tags ?: emptyList(),
            metadata = metadata
        )
        
        scope.launch {
            repository.trackBehaviorEvent(event)
        }
    }

    fun logMealView(userId: String, meal: Meal) {
        logEvent(userId, InteractionType.CLICK, meal)
    }

    fun logRecommendationClick(userId: String, meal: Meal, recommendationId: String) {
        logEvent(userId, InteractionType.CLICK, meal, mapOf("recommendationId" to recommendationId))
    }

    fun logRecommendationIgnore(userId: String, mealId: String, recommendationId: String) {
        // Implementation for logging ignored recommendations
    }
}
