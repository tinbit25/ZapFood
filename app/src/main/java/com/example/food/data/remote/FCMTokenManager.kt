package com.example.food.data.remote

import android.util.Log
import com.example.food.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * FCMTokenManager — Responsible for retrieving and synchronizing the FCM token.
 */
class FCMTokenManager(
    private val userRepository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Retrieves the current FCM token and uploads it to Firestore if the user is logged in.
     */
    fun syncToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCMTokenManager", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val userId = auth.currentUser?.uid
            
            if (userId != null && token != null) {
                scope.launch {
                    val success = userRepository.updateFcmToken(userId, token)
                    if (success) {
                        Log.d("FCMTokenManager", "FCM token synced successfully: $token")
                    } else {
                        Log.e("FCMTokenManager", "Failed to sync FCM token")
                    }
                }
            }
        }
    }

    /**
     * Updates the token in Firestore for the current user.
     */
    fun updateToken(token: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            scope.launch {
                userRepository.updateFcmToken(userId, token)
            }
        }
    }

    /**
     * Clears the token in Firestore (e.g., on logout).
     */
    fun clearToken() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            scope.launch {
                userRepository.updateFcmToken(userId, null)
            }
        }
    }
}
