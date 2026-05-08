package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.Notification
import com.example.food.data.model.NotificationStatus
import com.example.food.data.model.NotificationType
import com.example.food.data.repository.NotificationRepository
import com.example.food.domain.repository.INotificationRepository
import kotlinx.coroutines.flow.Flow

/**
 * NotificationUseCase — Business logic layer for notification operations.
 *
 * Encapsulates validation, notification construction, and delegates
 * persistence to [INotificationRepository].
 *
 * Follows the same constructor-injection pattern used by [SupportUseCase].
 */
class NotificationUseCase(
    private val notificationRepository: INotificationRepository = NotificationRepository()
) {

    // ── Realtime Observation ────────────────────────────────

    /**
     * Observe all notifications for a user, ordered newest-first.
     */
    fun observeNotifications(userId: String): Flow<Resource<List<Notification>>> {
        return notificationRepository.getUserNotifications(userId)
    }

    /**
     * Observe only unread notifications for a user.
     */
    fun observeUnread(userId: String): Flow<Resource<List<Notification>>> {
        return notificationRepository.getUnreadNotifications(userId)
    }

    /**
     * Observe unread count (for notification badge).
     */
    fun observeUnreadCount(userId: String): Flow<Resource<Int>> {
        return notificationRepository.getUnreadCount(userId)
    }

    // ── Notification Creation ───────────────────────────────

    /**
     * Create and persist a notification for a specific user.
     */
    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedOrderId: String? = null
    ): Resource<Unit> {
        if (userId.isBlank()) return Resource.Error("User ID cannot be empty")
        if (title.isBlank()) return Resource.Error("Notification title cannot be empty")
        if (message.isBlank()) return Resource.Error("Notification message cannot be empty")

        val notification = Notification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedOrderId = relatedOrderId
        )

        return notificationRepository.saveNotification(notification)
    }

    /**
     * Create an order-lifecycle notification with a pre-formatted title/message.
     */
    suspend fun sendOrderNotification(
        userId: String,
        orderId: String,
        type: NotificationType
    ): Resource<Unit> {
        val (title, message) = getOrderNotificationContent(type, orderId)
        return sendNotification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedOrderId = orderId
        )
    }

    // ── Read / Status Management ────────────────────────────

    /**
     * Mark a single notification as read.
     */
    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return notificationRepository.markAsRead(notificationId)
    }

    /**
     * Mark all notifications for a user as read.
     */
    suspend fun markAllAsRead(userId: String): Resource<Unit> {
        return notificationRepository.markAllAsRead(userId)
    }

    /**
     * Archive a notification (soft delete).
     */
    suspend fun archiveNotification(notificationId: String): Resource<Unit> {
        return notificationRepository.updateStatus(notificationId, NotificationStatus.ARCHIVED)
    }

    // ── Deletion ────────────────────────────────────────────

    /**
     * Permanently delete a single notification.
     */
    suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return notificationRepository.deleteNotification(notificationId)
    }

    /**
     * Clear all notifications for a user.
     */
    suspend fun clearAll(userId: String): Resource<Unit> {
        return notificationRepository.clearAllNotifications(userId)
    }

    // ── Private Helpers ─────────────────────────────────────

    /**
     * Returns (title, message) for each order notification type.
     */
    private fun getOrderNotificationContent(
        type: NotificationType,
        orderId: String
    ): Pair<String, String> {
        val shortId = orderId.takeLast(6).uppercase()
        return when (type) {
            NotificationType.ORDER_ACCEPTED ->
                "Order Accepted" to "Your order #$shortId has been accepted and is being processed."

            NotificationType.MEAL_PREPARING ->
                "Preparing Your Meal" to "Your order #$shortId is now being prepared."

            NotificationType.DELIVERY_ON_THE_WAY ->
                "On the Way!" to "Your order #$shortId is on its way to you."

            NotificationType.ORDER_DELIVERED ->
                "Order Delivered" to "Your order #$shortId has been delivered. Enjoy your meal!"

            NotificationType.SUPPORT_UPDATE ->
                "Support Update" to "There's an update on your support ticket for order #$shortId."

            NotificationType.ADMIN_ANNOUNCEMENT ->
                "Announcement" to "New announcement regarding order #$shortId."

            NotificationType.VENDOR_UPDATE ->
                "Vendor Update" to "The vendor has an update for order #$shortId."
        }
    }
}
