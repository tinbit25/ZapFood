package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.food.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CheckoutUiState(
    val orderType: OrderType = OrderType.DELIVERY,
    val deliveryDetails: DeliveryDetails = DeliveryDetails(),
    val takeawayDetails: TakeawayDetails = TakeawayDetails(),
    val dineInDetails: DineInDetails = DineInDetails(),
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CheckoutViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun setOrderType(type: OrderType) {
        _uiState.value = _uiState.value.copy(orderType = type)
    }

    fun updateDeliveryDetails(details: DeliveryDetails) {
        _uiState.value = _uiState.value.copy(deliveryDetails = details)
    }

    fun updateTakeawayDetails(details: TakeawayDetails) {
        _uiState.value = _uiState.value.copy(takeawayDetails = details)
    }

    fun updateDineInDetails(details: DineInDetails) {
        _uiState.value = _uiState.value.copy(dineInDetails = details)
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }
}
