package com.example.food.core.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * NotificationChannelManager — Handles the creation and management of notification channels.
 */
object NotificationChannelManager {
    const val CHANNEL_ORDERS_ID = "orders_channel"
    const val CHANNEL_SUPPORT_ID = "support_channel"
    const val CHANNEL_SYSTEM_ID = "system_channel"
    const val CHANNEL_PROMO_ID = "promo_channel"
    const val CHANNEL_VENDOR_ID = "vendor_channel"
    const val CHANNEL_CHEF_ID = "chef_channel"

    /**
     * Initializes all notification channels for the app.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ORDERS_ID,
                    "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications about your order status"
                },
                NotificationChannel(
                    CHANNEL_SUPPORT_ID,
                    "Support & Feedback",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications from our support team"
                },
                NotificationChannel(
                    CHANNEL_SYSTEM_ID,
                    "System Announcements",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "General updates and announcements"
                },
                NotificationChannel(
                    CHANNEL_PROMO_ID,
                    "Promotions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Special offers and discounts"
                },
                NotificationChannel(
                    CHANNEL_VENDOR_ID,
                    "Vendor Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates from your favorite vendors"
                },
                NotificationChannel(
                    CHANNEL_CHEF_ID,
                    "Chef Bookings",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for private chef sessions"
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }
}
