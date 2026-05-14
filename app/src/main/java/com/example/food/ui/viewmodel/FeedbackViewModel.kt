package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Feedback
import com.example.food.data.model.User
import com.example.food.domain.usecase.SupportUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

import com.example.food.data.repository.OrderRepository

class FeedbackViewModel(
    private val supportUseCase: SupportUseCase = SupportUseCase(),
    private val orderRepository: OrderRepository = OrderRepository()
) : ViewModel() {

    private val _feedbackListState = MutableStateFlow<Resource<List<Feedback>>>(Resource.Loading())
    val feedbackListState: StateFlow<Resource<List<Feedback>>> = _feedbackListState.asStateFlow()

    private val _vendorFeedbackState = MutableStateFlow<Resource<List<Feedback>>>(Resource.Loading())
    val vendorFeedbackState: StateFlow<Resource<List<Feedback>>> = _vendorFeedbackState.asStateFlow()

    fun submitFeedback(
        user: User,
        rating: Int,
        comment: String,
        orderId: String? = null,
        onResult: (Resource<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            var vendorId: String? = null
            var vendorName: String? = null

            if (orderId != null) {
                val orderResult = orderRepository.getOrderById(orderId)
                if (orderResult is Resource.Success<*>) {
                    val order = orderResult.data as? com.example.food.data.model.Order
                    vendorId = order?.vendorId
                    vendorName = order?.businessName
                }
            }

            val result = supportUseCase.submitFeedback(user, rating, comment, orderId, vendorId, vendorName)
            onResult(result)
        }
    }

    fun fetchAllFeedback(user: User) {
        viewModelScope.launch {
            supportUseCase.getFeedbackList(user).collect { result ->
                _feedbackListState.value = result
            }
        }
    }

    fun fetchVendorFeedback(vendorId: String) {
        viewModelScope.launch {
            supportUseCase.getVendorFeedback(vendorId).collect { result ->
                _vendorFeedbackState.value = result
            }
        }
    }
}
