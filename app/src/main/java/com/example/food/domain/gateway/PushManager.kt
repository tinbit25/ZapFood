package com.example.food.domain.gateway

import com.example.food.data.model.Notification

/**
 * PushManager — Domain-layer abstraction for push notification delivery.
 *
 * This interface decouples the notification architecture from any specific
 * push service (Firebase Cloud Messaging, OneSignal, etc.).
 *
 * Current status: Interface only — no FCM implementation yet.
 * When FCM is integrated, create `FcmPushManager` in `data/gateway/`.
 *
 * Usage flow:
 *   NotificationService → PushManager.sendPush() → FCM / local notification
 */
interface PushManager {

    /**
     * Register the current device for push notifications.
     * @return the device token, or null if registration failed.
     */
    suspend fun registerDevice(userId: String): String?

    /**
     * Unregister the current device (e.g. on logout).
     */
    suspend fun unregisterDevice(userId: String)

    /**
     * Send a push notification to a specific user's device(s).
     * The implementation decides whether to use FCM, local notification, or both.
     */
    suspend fun sendPush(notification: Notification)

    /**
     * Show a local (on-device) notification immediately.
     * Used when the app is in the foreground and a realtime update arrives.
     */
    suspend fun showLocalNotification(notification: Notification)

    /**
     * Check if push notifications are enabled by the user.
     */
    suspend fun isPushEnabled(): Boolean
}
