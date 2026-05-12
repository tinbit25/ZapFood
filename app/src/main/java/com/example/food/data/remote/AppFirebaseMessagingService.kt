package com.example.food.data.remote

import android.util.Log
import com.example.food.core.util.NotificationRenderer
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * AppFirebaseMessagingService — Handles incoming FCM messages and token refreshes.
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New token generated: $token")
        FCMTokenManager().updateToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCMService", "Message received from: ${remoteMessage.from}")

        // 1. Handle notification payload (sent via Firebase Console or simple API)
        remoteMessage.notification?.let {
            Log.d("FCMService", "Notification payload: title=${it.title}, body=${it.body}")
            NotificationRenderer(this).render(
                it.title ?: "ZapFood",
                it.body ?: "",
                remoteMessage.data["type"] ?: "SYSTEM",
                remoteMessage.data
            )
            return // Skip data payload if notification was handled
        }

        // 2. Handle data payload (sent via backend API for custom handling)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCMService", "Data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "ZapFood"
            val body = remoteMessage.data["body"] ?: remoteMessage.data["message"] ?: ""
            val type = remoteMessage.data["type"] ?: "SYSTEM"
            
            if (body.isNotEmpty()) {
                NotificationRenderer(this).render(title, body, type, remoteMessage.data)
            }
        }
    }
}
