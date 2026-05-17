package com.example.food.domain.manager

import com.example.food.data.model.Notification
import com.example.food.data.model.NotificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationStateManager(
    private val syncService: NotificationSyncService = NotificationSyncService()
) {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun updateNotifications(newNotifications: List<Notification>) {
        _notifications.value = newNotifications
        _unreadCount.value = newNotifications.count { !it.isRead }
    }

    fun markAsReadOptimistic(notificationId: String) {
        _notifications.update { current ->
            current.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(
                        isRead = true,
                        status = NotificationStatus.READ,
                        readAt = System.currentTimeMillis()
                    )
                } else notification
            }
        }
        _unreadCount.update { (it - 1).coerceAtLeast(0) }
        syncService.syncReadState(notificationId)
    }

    fun markAllAsReadOptimistic(userId: String) {
        _notifications.update { current ->
            current.map { notification ->
                if (!notification.isRead) {
                    notification.copy(
                        isRead = true,
                        status = NotificationStatus.READ,
                        readAt = System.currentTimeMillis()
                    )
                } else notification
            }
        }
        _unreadCount.value = 0
        syncService.syncAllReadStates(userId)
    }
}
