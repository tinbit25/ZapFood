package com.example.food.domain.manager

import android.util.Log
import com.example.food.core.util.Resource
import com.example.food.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSyncService(
    private val repository: NotificationRepository = NotificationRepository()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun syncReadState(notificationId: String) {
        scope.launch {
            val result = repository.markAsRead(notificationId)
            if (result is Resource.Error) {
                Log.e("NotificationSyncService", "Failed to mark notification as read: ${result.message}")
            }
        }
    }

    fun syncAllReadStates(userId: String) {
        scope.launch {
            val result = repository.markAllAsRead(userId)
            if (result is Resource.Error) {
                Log.e("NotificationSyncService", "Failed to mark all notifications as read: ${result.message}")
            }
        }
    }
}
