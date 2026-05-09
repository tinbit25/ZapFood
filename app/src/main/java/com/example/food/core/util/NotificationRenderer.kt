package com.example.food.core.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.food.MainActivity
import com.example.food.R

/**
 * NotificationRenderer — Handles the professional rendering of local notifications.
 * Supports grouping, branding, and specialized styling for an Ethiopian food platform.
 */
class NotificationRenderer(private val context: Context) {

    companion object {
        private const val GROUP_ORDERS = "com.example.food.ORDERS"
        private const val GROUP_SUPPORT = "com.example.food.SUPPORT"
        private const val GROUP_SYSTEM = "com.example.food.SYSTEM"
        
        private const val SUMMARY_ID_ORDERS = 1001
        private const val SUMMARY_ID_SUPPORT = 1002
        private const val SUMMARY_ID_SYSTEM = 1003
    }

    /**
     * Renders a notification with grouping and specific styling.
     */
    fun render(
        title: String,
        message: String,
        type: String,
        payload: Map<String, String> = emptyMap()
    ) {
        val channelId = when (type) {
            "ORDER" -> NotificationChannelManager.CHANNEL_ORDERS_ID
            "SUPPORT" -> NotificationChannelManager.CHANNEL_SUPPORT_ID
            else -> NotificationChannelManager.CHANNEL_SYSTEM_ID
        }

        val groupKey = when (type) {
            "ORDER" -> GROUP_ORDERS
            "SUPPORT" -> GROUP_SUPPORT
            else -> GROUP_SYSTEM
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            payload.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Ethiopian platform branding colors/style
        // Using purple_500 as primary brand color
        val brandColor = context.getColor(R.color.purple_500)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(brandColor)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setGroup(groupKey)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Show the actual notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Show/Update group summary
        renderSummary(notificationManager, channelId, groupKey, type)
    }

    /**
     * Renders or updates the group summary notification.
     */
    private fun renderSummary(
        notificationManager: NotificationManager,
        channelId: String,
        groupKey: String,
        type: String
    ) {
        val summaryId = when (type) {
            "ORDER" -> SUMMARY_ID_ORDERS
            "SUPPORT" -> SUMMARY_ID_SUPPORT
            else -> SUMMARY_ID_SYSTEM
        }

        val summaryTitle = when (type) {
            "ORDER" -> "Order Updates"
            "SUPPORT" -> "Support Messages"
            else -> "System Announcements"
        }

        val summaryBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(summaryTitle)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(context.getColor(R.color.purple_500))
            .setAutoCancel(true)

        notificationManager.notify(summaryId, summaryBuilder.build())
    }
}
