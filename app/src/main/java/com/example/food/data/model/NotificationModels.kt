package com.example.food.data.model

import java.util.UUID

/**
 * NotificationType — Categorizes notifications for routing and display.
 *
 * Order lifecycle:  ORDER_ACCEPTED → MEAL_PREPARING → DELIVERY_ON_THE_WAY → ORDER_DELIVERED
 * System:           SUPPORT_UPDATE, ADMIN_ANNOUNCEMENT, VENDOR_UPDATE
 */
enum class NotificationType {
    ORDER_ACCEPTED,
    MEAL_PREPARING,
    DELIVERY_ON_THE_WAY,
    ORDER_DELIVERED,
    SUPPORT_UPDATE,
    ADMIN_ANNOUNCEMENT,
    VENDOR_UPDATE
}

/**
 * NotificationStatus — Tracks the delivery/read lifecycle of a notification.
 */
enum class NotificationStatus {
    UNREAD,
    READ,
    ARCHIVED
}

/**
 * Notification — Core entity representing an in-app notification.
 *
 * Firestore collection: "notifications"
 * Document ID: [id]
 *
 * Designed for Firestore snapshot listeners to enable realtime updates.
 * All fields use defaults for Firestore deserialization compatibility.
 */
data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.ADMIN_ANNOUNCEMENT,
    val status: NotificationStatus = NotificationStatus.UNREAD,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedOrderId: String? = null
) {
    /**
     * Convenience check: is this an order-lifecycle notification?
     */
    val isOrderNotification: Boolean
        get() = type in listOf(
            NotificationType.ORDER_ACCEPTED,
            NotificationType.MEAL_PREPARING,
            NotificationType.DELIVERY_ON_THE_WAY,
            NotificationType.ORDER_DELIVERED
        )
}
