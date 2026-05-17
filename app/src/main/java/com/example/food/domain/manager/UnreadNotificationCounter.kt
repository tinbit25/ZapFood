package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnreadNotificationCounter {
    fun countUnread(notifications: List<Notification>): Int {
        return notifications.count { !it.isRead }
    }

    fun observeUnreadCount(stream: Flow<Resource<List<Notification>>>): Flow<Int> {
        return stream.map { resource ->
            when (resource) {
                is Resource.Success -> countUnread(resource.data ?: emptyList())
                else -> 0
            }
        }
    }
}
