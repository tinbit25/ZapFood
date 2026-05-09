package com.example.food.data.remote

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.food.MainActivity
import com.example.food.R
import com.example.food.core.util.NotificationChannelManager
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

        // Handle data payload if present
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCMService", "Data payload: ${remoteMessage.data}")
            // Process custom data here if needed
        }

        // Handle notification payload if present
        remoteMessage.notification?.let {
            NotificationRenderer(this).render(
                it.title ?: "Notification",
                it.body ?: "",
                remoteMessage.data["type"] ?: "SYSTEM",
                remoteMessage.data
            )
        }
    }
}
