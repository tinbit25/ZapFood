package com.example.food.domain.usecase

import com.example.food.data.model.Meal
import com.example.food.data.model.Order
import com.example.food.data.repository.InteractionRepository
import com.example.food.data.repository.MealMetadataRepository

class AnalyticsPreparationUseCase(
    private val interactionRepository: InteractionRepository = InteractionRepository(),
    private val metadataRepository: MealMetadataRepository = MealMetadataRepository()
) {

    /**
     * Call this periodically or triggered by specific events to update 
     * the global popularity scores of meals based on new orders.
     */
    suspend fun processOrderAnalytics(order: Order) {
        val timeOfDay = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 5..10 -> "Breakfast"
            in 11..15 -> "Lunch"
            in 16..22 -> "Dinner"
            else -> "Late Night"
        }

        order.items.forEach { item ->
            // Track implicit purchase for CF algorithms
            interactionRepository.trackPurchase(
                userId = order.customerId,
                mealId = item.mealId,
                timeOfDay = timeOfDay,
                quantity = item.quantity
            )

            // Increment the global popularity score
            // E.g., each order increases score by 1.0. Can apply time-decay later.
            metadataRepository.incrementPopularity(
                mealId = item.mealId,
                incrementValue = item.quantity.toDouble()
            )
        }
    }

    /**
     * Updates the local behavior intelligence mapping. 
     */
    fun extractOrderPatterns(order: Order, currentPatterns: Map<String, Int>): Map<String, Int> {
        val calendar = java.util.Calendar.getInstance()
        val isWeekend = calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY || 
                        calendar.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
        
        val newPatterns = currentPatterns.toMutableMap()
        val key = if (isWeekend) "weekend" else "weekday"
        
        newPatterns[key] = (newPatterns[key] ?: 0) + 1
        return newPatterns
    }
}
