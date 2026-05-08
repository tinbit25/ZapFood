package com.example.food.data.gateway

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.food.R
import com.example.food.core.util.NotificationChannelManager
import com.example.food.data.model.Notification
import com.example.food.data.remote.FCMTokenManager
import com.example.food.domain.gateway.PushManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * FcmPushManager — Implementation of [PushManager] using Firebase Cloud Messaging.
 */
class FcmPushManager(
    private val context: Context,
    private val tokenManager: FCMTokenManager = FCMTokenManager()
) : PushManager {

    override suspend fun registerDevice(userId: String): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            tokenManager.updateToken(token)
            token
        } catch (e: Exception) {
            Log.e("FcmPushManager", "Failed to register device", e)
            null
        }
    }

    override suspend fun unregisterDevice(userId: String) {
        try {
            tokenManager.clearToken()
            FirebaseMessaging.getInstance().deleteToken().await()
        } catch (e: Exception) {
            Log.e("FcmPushManager", "Failed to unregister device", e)
        }
    }

    override suspend fun sendPush(notification: Notification) {
        // Note: Client-to-client push is usually not recommended.
        // This would typically trigger a backend call to send the FCM.
        Log.i("FcmPushManager", "Push delivery for ${notification.id} should be triggered via backend.")
    }

    override suspend fun showLocalNotification(notification: Notification) {
        val channelId = when {
            notification.isOrderNotification -> NotificationChannelManager.CHANNEL_ORDERS_ID
            notification.type.name.contains("SUPPORT") -> NotificationChannelManager.CHANNEL_SUPPORT_ID
            else -> NotificationChannelManager.CHANNEL_SYSTEM_ID
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    override suspend fun isPushEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }
}
