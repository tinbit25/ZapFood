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

class FeedbackViewModel(
    private val supportUseCase: SupportUseCase = SupportUseCase()
) : ViewModel() {

    private val _feedbackListState = MutableStateFlow<Resource<List<Feedback>>>(Resource.Loading())
    val feedbackListState: StateFlow<Resource<List<Feedback>>> = _feedbackListState.asStateFlow()

    fun submitFeedback(
        user: User,
        rating: Int,
        comment: String,
        orderId: String? = null,
        onResult: (Resource<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = supportUseCase.submitFeedback(user, rating, comment, orderId)
            onResult(result)
        }
    }

    fun fetchAllFeedback(user: User) {
        viewModelScope.launch {
            supportUseCase.getFeedbackList(user).collect {
                _feedbackListState.value = it
            }
        }
    }
}
