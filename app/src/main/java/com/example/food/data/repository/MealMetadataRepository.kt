package com.example.food.data.repository

import com.example.food.data.model.Meal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MealMetadataRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Increment the popularity score of a specific meal.
     */
    suspend fun incrementPopularity(mealId: String, incrementValue: Double = 1.0) {
        try {
            val docRef = firestore.collection("meals").document(mealId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentScore = snapshot.getDouble("popularityScore") ?: 0.0
                transaction.update(docRef, "popularityScore", currentScore + incrementValue)
            }.await()
        } catch (e: Exception) {
            // Ignore for background task
        }
    }

    /**
     * Normalize tags for a meal.
     */
    suspend fun normalizeTags(meal: Meal, tags: List<String>) {
        try {
            val docRef = firestore.collection("meals").document(meal.id)
            docRef.update("tags", tags).await()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
