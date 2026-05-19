package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.UserFoodPreference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserPreferenceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observePreferences(userId: String): Flow<Resource<UserFoodPreference>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(Resource.Success(UserFoodPreference()))
            close()
            return@callbackFlow
        }
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(userId).collection("preferences").document("profile")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to listen for preferences"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    val prefs = snapshot.toObject(UserFoodPreference::class.java)
                    if (prefs != null) {
                        trySend(Resource.Success(prefs))
                    } else {
                        trySend(Resource.Error("Failed to parse preferences"))
                    }
                } else {
                    // Create default if it doesn't exist
                    trySend(Resource.Success(UserFoodPreference(userId = userId)))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun updatePreferences(preferences: UserFoodPreference): Resource<Unit> {
        if (preferences.userId.isBlank()) {
            return Resource.Error("User ID cannot be blank")
        }
        return try {
            firestore.collection("users").document(preferences.userId)
                .collection("preferences").document("profile")
                .set(preferences).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update preferences")
        }
    }
    
    suspend fun getPreferences(userId: String): Resource<UserFoodPreference> {
        if (userId.isBlank()) {
            return Resource.Success(UserFoodPreference())
        }
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("preferences").document("profile").get().await()
            
            if (snapshot.exists()) {
                val prefs = snapshot.toObject(UserFoodPreference::class.java)
                if (prefs != null) {
                    Resource.Success(prefs)
                } else {
                    Resource.Error("Failed to parse preferences")
                }
            } else {
                Resource.Success(UserFoodPreference(userId = userId))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get preferences")
        }
    }
}
