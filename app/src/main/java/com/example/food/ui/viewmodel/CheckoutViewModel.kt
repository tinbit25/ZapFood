package com.example.food.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.OrderRepository
import com.example.food.domain.manager.OrderStateSynchronizer
import com.example.food.domain.manager.UnifiedOrderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val orderType: OrderType = OrderType.DELIVERY,
    // Unified info blocks matching the Order model
    val deliveryInfo: DeliveryDetails = DeliveryDetails(),
    val pickupInfo: TakeawayDetails = TakeawayDetails(),
    val dineInInfo: DineInDetails = DineInDetails(),
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val isLoading: Boolean = false,
    val error: String? = null,
    val placedOrder: Order? = null
)

class CheckoutViewModel @Inject constructor() : ViewModel() {

    private val orderRepository = OrderRepository()
    private val unifiedOrderManager = UnifiedOrderManager(
        orderRepository = orderRepository,
        synchronizer = OrderStateSynchronizer(orderRepository)
    )

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    var pointsToRedeem by mutableIntStateOf(0)

    fun setOrderType(type: OrderType) {
        _uiState.value = _uiState.value.copy(orderType = type, error = null)
    }

    fun updateDeliveryInfo(details: DeliveryDetails) {
        _uiState.value = _uiState.value.copy(deliveryInfo = details)
    }

    fun updatePickupInfo(details: TakeawayDetails) {
        _uiState.value = _uiState.value.copy(pickupInfo = details)
    }

    fun updateDineInInfo(details: DineInDetails) {
        _uiState.value = _uiState.value.copy(dineInInfo = details)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun setPlacingOrder(placing: Boolean) {
        _isPlacingOrder.value = placing
    }

    /**
     * Builds and submits the order through [UnifiedOrderManager].
     * Caller supplies the partially-constructed [Order]; the manager
     * will validate type-specific info and persist it.
     */
    fun placeOrder(order: Order, onResult: (Resource<Order>) -> Unit) {
        viewModelScope.launch {
            _isPlacingOrder.value = true
            _uiState.value = _uiState.value.copy(error = null)

            // Attach the unified info blocks from UI state
            val state = _uiState.value
            val enrichedOrder = order.copy(
                orderType = state.orderType,
                paymentMethod = state.paymentMethod,
                deliveryInfo = state.deliveryInfo.takeIf { state.orderType == OrderType.DELIVERY },
                pickupInfo   = state.pickupInfo.takeIf   { state.orderType == OrderType.TAKEAWAY },
                dineInInfo   = state.dineInInfo.takeIf   { state.orderType == OrderType.DINE_IN }
            )

            val result = unifiedOrderManager.placeOrder(enrichedOrder)
            _isPlacingOrder.value = false

            if (result is Resource.Error) {
                _uiState.value = _uiState.value.copy(error = result.message)
            } else if (result is Resource.Success) {
                _uiState.value = _uiState.value.copy(placedOrder = result.data)
            }

            onResult(result)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
