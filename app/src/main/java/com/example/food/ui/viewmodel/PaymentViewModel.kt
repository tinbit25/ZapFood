package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Payment
import com.example.food.data.model.PaymentMethod
import com.example.food.domain.usecase.PaymentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val payment: Payment) : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class PaymentViewModel(
    private val paymentUseCase: PaymentUseCase = PaymentUseCase()
) : ViewModel() {

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun initiatePayment(orderId: String, userId: String, amount: Double, method: PaymentMethod) {
        viewModelScope.launch {
            paymentUseCase.initiatePayment(orderId, userId, amount, method).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _paymentState.value = PaymentState.Loading
                    is Resource.Success -> _paymentState.value = PaymentState.Success(resource.data!!)
                    is Resource.Error -> _paymentState.value = PaymentState.Error(resource.message ?: "Payment failed")
                }
            }
        }
    }

    fun retryPayment(orderId: String, userId: String, amount: Double, method: PaymentMethod) {
        // For retry, we reset state and initiate again
        _paymentState.value = PaymentState.Idle
        initiatePayment(orderId, userId, amount, method)
    }

    fun resetState() {
        _paymentState.value = PaymentState.Idle
    }
}
