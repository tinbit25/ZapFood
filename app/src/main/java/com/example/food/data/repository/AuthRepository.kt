package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registerUser(user: User): Resource<User> {
        val docId = if (user.userId.isNotEmpty()) user.userId else user.id.toString()
        return try {
            firestore.collection("users").document(docId).set(user).await()
            Resource.Success(user)
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
