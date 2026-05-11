package com.example.food.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class InteractionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun trackMealClick(userId: String, mealId: String) {
        val event = mapOf(
            "userId" to userId,
            "mealId" to mealId,
            "type" to "CLICK",
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("interactions").add(event).await()
        } catch (e: Exception) {
            // Ignore for background task
        }
    }

    suspend fun trackFavorite(userId: String, mealId: String, isFavorite: Boolean) {
        val event = mapOf(
            "userId" to userId,
            "mealId" to mealId,
            "type" to if (isFavorite) "FAVORITE_ADD" else "FAVORITE_REMOVE",
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("interactions").add(event).await()
        } catch (e: Exception) {}
    }

    suspend fun trackPurchase(userId: String, mealId: String, timeOfDay: String, quantity: Int) {
        val event = mapOf(
            "userId" to userId,
            "mealId" to mealId,
            "type" to "PURCHASE",
            "timeOfDay" to timeOfDay,
            "quantity" to quantity,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("interactions").add(event).await()
        } catch (e: Exception) {}
    }

    suspend fun trackRating(userId: String, mealId: String, rating: Float) {
        val event = mapOf(
            "userId" to userId,
            "mealId" to mealId,
            "type" to "RATING",
            "rating" to rating,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("interactions").add(event).await()
        } catch (e: Exception) {}
    }

    suspend fun trackBehaviorEvent(event: com.example.food.data.model.AnalyticsEvent) {
        try {
            firestore.collection("user_behavior").document(event.id).set(event).await()
        } catch (e: Exception) {
            // Background tracking, fail silently
        }
    }
}
