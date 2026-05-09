package com.example.food.data.remote

import com.example.food.core.util.Resource
import com.example.food.data.model.Notification
import com.example.food.data.model.NotificationType
import com.example.food.data.repository.NotificationRepository
import com.example.food.domain.gateway.PushManager
import com.example.food.domain.repository.INotificationRepository

/**
 * NotificationService — Orchestrates notification persistence and delivery.
 *
 * This service sits between the use case layer and the push delivery layer.
 * It coordinates:
 *   1. Persisting the notification to Firestore (via [INotificationRepository])
 *   2. Triggering push delivery (via [PushManager], when implemented)
 *
 * Architecture:
 *   UseCase → NotificationService → Repository (Firestore)
 *                                  → PushManager (FCM / Local — future)
 *
 * The [pushManager] is nullable to allow operation without push notifications
 * during the foundational phase.
 */
class NotificationService(
    private val notificationRepository: INotificationRepository = NotificationRepository(),
    private val pushManager: PushManager? = null  // Will be injected when FCM is set up
) {

    /**
     * Create, persist, and optionally push-deliver a notification.
     */
    suspend fun notify(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        relatedOrderId: String? = null
    ): Resource<Unit> {
        val notification = Notification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedOrderId = relatedOrderId
        )

        // Step 1: Persist to Firestore
        val saveResult = notificationRepository.saveNotification(notification)
        if (saveResult is Resource.Error) return saveResult

        // Step 2: Trigger push delivery (when available)
        pushManager?.let { manager ->
            try {
                manager.sendPush(notification)
            } catch (e: Exception) {
                // Push failure should not fail the overall notification
                // Log but don't propagate the error
                println("PUSH_DELIVERY_FAILED: ${e.message}")
            }
        }

        return Resource.Success(Unit)
    }

    /**
     * Send an order-lifecycle notification with auto-generated content.
     */
    suspend fun notifyOrderUpdate(
        userId: String,
        orderId: String,
        type: NotificationType
    ): Resource<Unit> {
        val (title, message) = buildOrderContent(type, orderId)
        return notify(
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedOrderId = orderId
        )
    }

    /**
     * Send a system-wide announcement to a specific user.
     */
    suspend fun notifyAnnouncement(
        userId: String,
        title: String,
        message: String
    ): Resource<Unit> {
        return notify(
            userId = userId,
            title = title,
            message = message,
            type = NotificationType.ADMIN_ANNOUNCEMENT
        )
    }

    /**
     * Send a support ticket update notification.
     */
    suspend fun notifySupportUpdate(
        userId: String,
        ticketId: String,
        updateMessage: String
    ): Resource<Unit> {
        return notify(
            userId = userId,
            title = "Support Update",
            message = updateMessage,
            type = NotificationType.SUPPORT_UPDATE,
            relatedOrderId = ticketId  // Reuse field for ticket reference
        )
    }

    // ── Private Helpers ─────────────────────────────────────

    private fun buildOrderContent(
        type: NotificationType,
        orderId: String
    ): Pair<String, String> {
        val shortId = orderId.takeLast(6).uppercase()
        return when (type) {
            NotificationType.ORDER_ACCEPTED ->
                "Order Accepted ✓" to "Your order #$shortId has been accepted!"

            NotificationType.MEAL_PREPARING ->
                "Preparing Your Meal 🍳" to "Your order #$shortId is being prepared."

            NotificationType.DELIVERY_ON_THE_WAY ->
                "On the Way! 🚗" to "Your order #$shortId is on its way."

            NotificationType.ORDER_DELIVERED ->
                "Delivered! 🎉" to "Your order #$shortId has been arrived. Enjoy!"

            NotificationType.ORDER_READY ->
                "Order Ready! 🍽️" to "Your order #$shortId is ready for pickup/delivery."

            NotificationType.ORDER_CANCELLED ->
                "Order Cancelled" to "Your order #$shortId has been cancelled."

            else -> "Order Update" to "Update for order #$shortId."
        }
    }
}
