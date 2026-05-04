package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registerUser(user: User, password: String): Resource<User> {
        return try {
            // Step 1: Create Firebase Auth account — this logs in the new user automatically
            val authResult = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUid = authResult.user?.uid
                ?: return Resource.Error("Firebase Auth failed: no UID returned")

            // Step 2: Build the Firestore document with the real Firebase UID
            val userToSave = user.copy(userId = firebaseUid)

            // Step 3: Save profile to Firestore
            firestore.collection("users").document(firebaseUid).set(userToSave).await()
            Resource.Success(userToSave)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed")
        }
    }

    suspend fun saveSession(session: AuthSession): Resource<Unit> {
        return try {
            firestore.collection("sessions").document(session.sessionId).set(session).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save session")
        }
    }

    suspend fun removeSession(sessionId: String): Resource<Unit> {
        return try {
            firestore.collection("sessions").document(sessionId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove session")
        }
    }

    suspend fun removeAllUserSessions(userId: String): Resource<Unit> {
        return try {
            val sessions = firestore.collection("sessions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val batch = firestore.batch()
            for (doc in sessions.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove all sessions")
        }
    }

    suspend fun saveResetToken(resetToken: ResetToken): Resource<Unit> {
        return try {
            firestore.collection("reset_tokens").document(resetToken.token).set(resetToken).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save reset token")
        }
    }

    suspend fun getResetToken(token: String): ResetToken? {
        return try {
            val doc = firestore.collection("reset_tokens").document(token).get().await()
            doc.toObject(ResetToken::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserPassword(userId: String, passwordHash: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId).update("passwordHash", passwordHash).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update password")
        }
    }
}
