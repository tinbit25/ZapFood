package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.OrderTimeline
import com.example.food.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * OrderTrackingViewModel — Manages the realtime state for order tracking.
 */
class OrderTrackingViewModel(
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _timelineState = MutableStateFlow<Resource<OrderTimeline>>(Resource.Loading())
    val timelineState: StateFlow<Resource<OrderTimeline>> = _timelineState.asStateFlow()

    fun observeOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.observeOrderTimeline(orderId).collect {
                _timelineState.value = it
            }
        }
    }

    fun checkIn(orderId: String, tableNumber: String, onResult: (Resource<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = orderRepository.checkInToTable(orderId, tableNumber)
            onResult(result)
        }
    }
}
