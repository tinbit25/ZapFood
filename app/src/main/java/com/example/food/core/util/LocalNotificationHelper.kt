package com.example.food.core.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.food.MainActivity
import com.example.food.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

/**
 * LocalNotificationHelper — Utility to show real system notifications for testing.
 */
object LocalNotificationHelper {

    fun showOrderNotification(context: Context, userId: String, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // 1. Persist to Firestore so it appears in the Notification Screen list
        val notification = com.example.food.data.model.Notification(
            userId = userId,
            title = title,
            message = message,
            type = com.example.food.data.model.NotificationType.ORDER_STATUS_UPDATE
        )
        
        // Use GlobalScope or similar for simple testing persistence
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.example.food.data.repository.NotificationRepository().saveNotification(notification)
        }

        // 2. Setup intent to go to Notifications Page when clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", "SYSTEM") // "SYSTEM" maps to Screen.Notifications.route in MainActivity
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ORDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder)
    }
}
