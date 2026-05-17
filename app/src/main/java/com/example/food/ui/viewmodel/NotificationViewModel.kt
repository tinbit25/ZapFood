package com.example.food.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Notification
import com.example.food.domain.usecase.NotificationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import com.example.food.domain.manager.NotificationStateManager

/**
 * NotificationViewModel — Manages the state for the notification center.
 * Handles realtime synchronization, pagination, and unread count tracking.
 */
class NotificationViewModel(
    private val notificationUseCase: NotificationUseCase = NotificationUseCase()
) : ViewModel() {

    private val stateManager = NotificationStateManager()

    private val _notificationsState = mutableStateOf<Resource<List<Notification>>>(Resource.Loading())
    val notificationsState: State<Resource<List<Notification>>> = _notificationsState

    private val _unreadCountState = mutableStateOf<Int>(0)
    val unreadCountState: State<Int> = _unreadCountState

    private var observationJob: Job? = null
    private var unreadCountJob: Job? = null
    
    private val allNotifications = mutableListOf<Notification>()
    private var lastTimestamp: Long? = null
    private var isLoadingMore = false

    /**
     * Start observing notifications for the current user.
     */
    fun startObserving(userId: String) {
        if (userId.isBlank()) return
        
        // Reset state
        allNotifications.clear()
        lastTimestamp = null
        
        // Observe Unread Count
        unreadCountJob?.cancel()
        unreadCountJob = notificationUseCase.observeUnreadCount(userId)
            .onEach { resource ->
                if (resource is Resource.Success) {
                    // Update state manager with latest DB unread count if we are not optimistic
                    val dbCount = resource.data ?: 0
                    if (stateManager.unreadCount.value != dbCount) {
                        _unreadCountState.value = dbCount
                    }
                }
            }.launchIn(viewModelScope)

        // Initial Load
        loadNotifications(userId)
    }

    /**
     * Load notifications with pagination.
     */
    private fun loadNotifications(userId: String) {
        observationJob?.cancel()
        observationJob = notificationUseCase.observeNotifications(userId, limit = 20, lastTimestamp = lastTimestamp)
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val newData = resource.data ?: emptyList()
                        allNotifications.clear()
                        allNotifications.addAll(newData)
                        stateManager.updateNotifications(newData)
                        _notificationsState.value = Resource.Success(stateManager.notifications.value)
                        _unreadCountState.value = stateManager.unreadCount.value
                        isLoadingMore = false
                    }
                    is Resource.Error -> {
                        _notificationsState.value = Resource.Error(resource.message ?: "An error occurred")
                        isLoadingMore = false
                    }
                    is Resource.Loading -> {
                        if (allNotifications.isEmpty()) {
                            _notificationsState.value = Resource.Loading()
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    /**
     * Mark a notification as read.
     */
    fun markAsRead(notificationId: String) {
        stateManager.markAsReadOptimistic(notificationId)
        _notificationsState.value = Resource.Success(stateManager.notifications.value)
        _unreadCountState.value = stateManager.unreadCount.value
    }

    /**
     * Mark all as read.
     */
    fun markAllAsRead(userId: String) {
        stateManager.markAllAsReadOptimistic(userId)
        _notificationsState.value = Resource.Success(stateManager.notifications.value)
        _unreadCountState.value = stateManager.unreadCount.value
    }

    /**
     * Delete a notification.
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationUseCase.deleteNotification(notificationId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        observationJob?.cancel()
        unreadCountJob?.cancel()
    }
}
