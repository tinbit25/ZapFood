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

    private val _latestBroadcastState = mutableStateOf<com.example.food.data.model.SystemBroadcast?>(null)
    val latestBroadcastState: State<com.example.food.data.model.SystemBroadcast?> = _latestBroadcastState

    private var observationJob: Job? = null
    private var unreadCountJob: Job? = null
    private var broadcastJob: Job? = null
    
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
                    _unreadCountState.value = resource.data ?: 0
                }
            }.launchIn(viewModelScope)

        // Initial Load
        loadNotifications(userId)
        
        // Start observing broadcasts
        startObservingBroadcasts()
    }

    fun startObservingBroadcasts() {
        broadcastJob?.cancel()
        broadcastJob = notificationUseCase.observeLatestBroadcast()
            .onEach { broadcast ->
                _latestBroadcastState.value = broadcast
            }.launchIn(viewModelScope)
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
     * Mark a single notification as read — updates local state immediately (optimistic)
     * and persists the read receipt to Firestore in the background.
     */
    fun markAsRead(notificationId: String) {
        // 1. Optimistic local update for instant UI response
        stateManager.markAsReadOptimistic(notificationId)
        _notificationsState.value = Resource.Success(stateManager.notifications.value)
        // 2. Persist to Firestore so the badge clears on all devices/sessions
        viewModelScope.launch {
            notificationUseCase.markAsRead(notificationId)
        }
    }

    /**
     * Mark all notifications as read — updates local state immediately and
     * persists all read receipts to Firestore to clear the badge globally.
     */
    fun markAllAsRead(userId: String) {
        // 1. Optimistic local update
        stateManager.markAllAsReadOptimistic(userId)
        _notificationsState.value = Resource.Success(stateManager.notifications.value)
        // 2. Persist to Firestore
        viewModelScope.launch {
            notificationUseCase.markAllAsRead(userId)
        }
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
        broadcastJob?.cancel()
    }
}
