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
            showNotification(
                it.title ?: "Notification",
                it.body ?: "",
                remoteMessage.data["type"] ?: "SYSTEM"
            )
        }
    }

    private fun showNotification(title: String, body: String, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (type) {
            "ORDER" -> NotificationChannelManager.CHANNEL_ORDERS_ID
            "SUPPORT" -> NotificationChannelManager.CHANNEL_SUPPORT_ID
            else -> NotificationChannelManager.CHANNEL_SYSTEM_ID
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this exists or use a generic one
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
