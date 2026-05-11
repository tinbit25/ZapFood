package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.repository.RecommendationRepository
import com.example.food.domain.model.ScoredMealResponse
import com.example.food.domain.model.AIRecommendationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.food.data.model.Meal

sealed class RecommendationState {
    object Initial : RecommendationState()
    object Loading : RecommendationState()
    data class Success(
        val personalized: List<ScoredMealResponse>,
        val reasoning: String = "",
        val nutritionSummary: String = ""
    ) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
    
    // Additional states for Cart
    data class CartSuggestionsLoaded(val suggestions: List<ScoredMealResponse>) : RecommendationState()
}

class RecommendationViewModel(
    private val repository: RecommendationRepository = RecommendationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationState>(RecommendationState.Initial)
    val uiState: StateFlow<RecommendationState> = _uiState.asStateFlow()

    fun loadHomeRecommendations(userId: String) {
        viewModelScope.launch {
            _uiState.value = RecommendationState.Loading
            try {
                val result = repository.getAIRecommendations(userId)

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    _uiState.value = RecommendationState.Success(
                        personalized = data?.recommendations ?: emptyList(),
                        reasoning = data?.reasoning ?: "",
                        nutritionSummary = data?.nutritionSummary ?: ""
                    )
                } else {
                    _uiState.value = RecommendationState.Error(result.exceptionOrNull()?.message ?: "Failed to load recommendations")
                }
            } catch (e: Exception) {
                _uiState.value = RecommendationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _cartSuggestionsState = MutableStateFlow<RecommendationState>(RecommendationState.Initial)
    val cartSuggestionsState: StateFlow<RecommendationState> = _cartSuggestionsState.asStateFlow()

    fun loadCartSuggestions(userId: String, cartMeals: List<Meal>) {
        if (cartMeals.isEmpty()) {
            _cartSuggestionsState.value = RecommendationState.CartSuggestionsLoaded(emptyList())
            return
        }
        viewModelScope.launch {
            _cartSuggestionsState.value = RecommendationState.Loading
            try {
                val cartMealIds = cartMeals.map { it.id }
                val result = repository.getCartSuggestions(userId, cartMealIds)
                if (result.isSuccess) {
                    _cartSuggestionsState.value = RecommendationState.CartSuggestionsLoaded(result.getOrNull() ?: emptyList())
                } else {
                    _cartSuggestionsState.value = RecommendationState.Error(result.exceptionOrNull()?.message ?: "Failed to load")
                }
            } catch (e: Exception) {
                _cartSuggestionsState.value = RecommendationState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun trackAnalyticsEvent(userId: String, eventType: String, mealId: String? = null, context: String? = null) {
        viewModelScope.launch {
            repository.trackAnalyticsEvent(userId, eventType, mealId, context)
        }
    }
}
