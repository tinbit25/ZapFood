package com.example.food.core.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.food.MainActivity
import com.example.food.R

import com.example.food.data.datastore.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * NotificationRenderer — Handles the professional rendering of local notifications.
 * Supports grouping, branding, and specialized styling for an Ethiopian food platform.
 */
class NotificationRenderer(private val context: Context) {

    private val settingsRepository = SettingsRepository(context)

    companion object {
        private const val GROUP_ORDERS = "com.example.food.ORDERS"
        private const val GROUP_SUPPORT = "com.example.food.SUPPORT"
        private const val GROUP_SYSTEM = "com.example.food.SYSTEM"
        private const val GROUP_PROMO = "com.example.food.PROMO"
        private const val GROUP_VENDOR = "com.example.food.VENDOR"
        private const val GROUP_CHEF = "com.example.food.CHEF"
        
        private const val SUMMARY_ID_ORDERS = 1001
        private const val SUMMARY_ID_SUPPORT = 1002
        private const val SUMMARY_ID_SYSTEM = 1003
        private const val SUMMARY_ID_PROMO = 1004
        private const val SUMMARY_ID_VENDOR = 1005
        private const val SUMMARY_ID_CHEF = 1006
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
        // 1. Check if user has enabled this category
        val isEnabled = runBlocking {
            when (type) {
                "ORDER" -> settingsRepository.notificationsOrder.first()
                "SUPPORT" -> settingsRepository.notificationsSupport.first()
                "PROMO" -> settingsRepository.notificationsPromo.first()
                "VENDOR" -> settingsRepository.notificationsVendor.first()
                "CHEF" -> settingsRepository.notificationsChef.first()
                else -> settingsRepository.notificationsSystem.first()
            }
        }

        if (!isEnabled) {
            Log.d("NotificationRenderer", "Notification suppressed by user settings: type=$type")
            return
        }

        val channelId = when (type) {
            "ORDER" -> NotificationChannelManager.CHANNEL_ORDERS_ID
            "SUPPORT" -> NotificationChannelManager.CHANNEL_SUPPORT_ID
            "PROMO" -> NotificationChannelManager.CHANNEL_PROMO_ID
            "VENDOR" -> NotificationChannelManager.CHANNEL_VENDOR_ID
            "CHEF" -> NotificationChannelManager.CHANNEL_CHEF_ID
            else -> NotificationChannelManager.CHANNEL_SYSTEM_ID
        }

        val groupKey = when (type) {
            "ORDER" -> GROUP_ORDERS
            "SUPPORT" -> GROUP_SUPPORT
            "PROMO" -> GROUP_PROMO
            "VENDOR" -> GROUP_VENDOR
            "CHEF" -> GROUP_CHEF
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
        
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

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
            "PROMO" -> SUMMARY_ID_PROMO
            "VENDOR" -> SUMMARY_ID_VENDOR
            "CHEF" -> SUMMARY_ID_CHEF
            else -> SUMMARY_ID_SYSTEM
        }

        val summaryTitle = when (type) {
            "ORDER" -> "Order Updates"
            "SUPPORT" -> "Support Messages"
            "PROMO" -> "Promotions"
            "VENDOR" -> "Vendor Updates"
            "CHEF" -> "Chef Bookings"
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
