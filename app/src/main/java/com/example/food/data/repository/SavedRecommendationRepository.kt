package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SavedRecommendationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeSavedMealIds(userId: String): Flow<Resource<List<String>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(userId)
            .collection("savedPicks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to listen for saved picks"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }
                    trySend(Resource.Success(ids))
                } else {
                    trySend(Resource.Success(emptyList()))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun saveMeal(userId: String, mealId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("savedPicks").document(mealId)
                .set(mapOf("savedAt" to System.currentTimeMillis())).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save meal")
        }
    }

    suspend fun unsaveMeal(userId: String, mealId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("savedPicks").document(mealId)
                .delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove saved meal")
        }
    }
}
