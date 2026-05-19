package com.example.food.domain.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.Notification
import com.example.food.data.model.NotificationStatus
import kotlinx.coroutines.flow.Flow

/**
 * INotificationRepository — Domain-layer abstraction for notification persistence.
 *
 * Implementations should:
 * - Use Firestore snapshot listeners for realtime [Flow]-based queries
 * - Store notifications in a "notifications" collection
 * - Support future push notification integration via [PushManager]
 */
interface INotificationRepository {

    /**
     * Observe notifications for a user in realtime, with pagination support.
     * @param limit maximum number of notifications to fetch.
     * @param lastTimestamp optional timestamp to start fetching after (for pagination).
     */
    fun getUserNotifications(
        userId: String,
        limit: Int = 20,
        lastTimestamp: Long? = null
    ): Flow<Resource<List<Notification>>>

    /**
     * Observe only unread notifications for a user in realtime.
     */
    fun getUnreadNotifications(userId: String): Flow<Resource<List<Notification>>>

    /**
     * Observe the count of unread notifications (for badge display).
     */
    fun getUnreadCount(userId: String): Flow<Resource<Int>>

    /**
     * Persist a new notification to Firestore.
     */
    suspend fun saveNotification(notification: Notification): Resource<Unit>

    /**
     * Mark a single notification as read.
     */
    suspend fun markAsRead(notificationId: String): Resource<Unit>

    /**
     * Mark all of a user's notifications as read.
     */
    suspend fun markAllAsRead(userId: String): Resource<Unit>

    /**
     * Update notification status (UNREAD → READ → ARCHIVED).
     */
    suspend fun updateStatus(notificationId: String, status: NotificationStatus): Resource<Unit>

    /**
     * Delete a single notification.
     */
    suspend fun deleteNotification(notificationId: String): Resource<Unit>

    /**
     * Delete all notifications for a user.
     */
    suspend fun clearAllNotifications(userId: String): Resource<Unit>

    /**
     * Observe the latest active system broadcast in realtime.
     */
    fun observeLatestBroadcast(): Flow<com.example.food.data.model.SystemBroadcast?>
}
