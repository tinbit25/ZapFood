package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.example.food.data.model.User
import com.example.food.domain.usecase.OrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderViewModel(
    private val orderUseCase: OrderUseCase = OrderUseCase()
) : ViewModel() {

    private val _userOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val userOrders: StateFlow<Resource<List<Order>>> = _userOrders.asStateFlow()

    private val _vendorOrders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val vendorOrders: StateFlow<Resource<List<Order>>> = _vendorOrders.asStateFlow()

    fun fetchUserOrders(userId: String) {
        viewModelScope.launch {
            orderUseCase.getMyOrders(userId).collect {
                _userOrders.value = it
            }
        }
    }

    fun fetchVendorOrders(vendorId: String) {
        viewModelScope.launch {
            orderUseCase.getIncomingOrders(vendorId).collect {
                _vendorOrders.value = it
            }
        }
    }

    fun placeOrder(user: User, mealIds: List<String>, mealPlanId: String? = null, onResult: (Resource<Order>) -> Unit) {
        viewModelScope.launch {
            val result = orderUseCase.placeOrder(user, mealIds, mealPlanId)
            onResult(result)
        }
    }

    fun updateStatus(user: User, orderId: String, nextStatus: OrderStatus, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = orderUseCase.updateOrderStatus(user, orderId, nextStatus)
            onResult(result)
        }
    }

    fun cancelOrder(user: User, orderId: String, onResult: (Resource<Unit>) -> Unit) {
        updateStatus(user, orderId, OrderStatus.CANCELLED, onResult)
    }

    fun acceptOrder(user: User, orderId: String, onResult: (Resource<Unit>) -> Unit) {
        updateStatus(user, orderId, OrderStatus.ACCEPTED, onResult)
    }
}
