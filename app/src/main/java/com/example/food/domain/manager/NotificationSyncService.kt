package com.example.food.domain.manager

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
            repository.markAsRead(notificationId)
        }
    }

    fun syncAllReadStates(userId: String) {
        scope.launch {
            repository.markAllAsRead(userId)
        }
    }
}
