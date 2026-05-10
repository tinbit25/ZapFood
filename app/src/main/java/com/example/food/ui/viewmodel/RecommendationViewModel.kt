package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.repository.RecommendationRepository
import com.example.food.domain.model.ScoredMeal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.food.domain.model.ComboRecommendation
import com.example.food.data.model.Meal

sealed class RecommendationState {
    object Initial : RecommendationState()
    object Loading : RecommendationState()
    data class Success(
        val personalized: List<ScoredMeal>,
        val trending: List<ScoredMeal>,
        val fastingPicks: List<ScoredMeal>
    ) : RecommendationState()
    data class Error(val message: String) : RecommendationState()
    
    // Additional states for Cart
    data class CartSuggestionsLoaded(val suggestions: List<ComboRecommendation>) : RecommendationState()
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
                // Fetch in parallel ideally, but doing sequentially for safety here
                val personalizedResult = repository.getPersonalizedRecommendations(userId)
                val trendingResult = repository.getTrendingMeals(userId)
                val fastingResult = repository.getFastingPicks(userId)

                if (personalizedResult.isSuccess && trendingResult.isSuccess) {
                    _uiState.value = RecommendationState.Success(
                        personalized = personalizedResult.getOrNull() ?: emptyList(),
                        trending = trendingResult.getOrNull() ?: emptyList(),
                        fastingPicks = fastingResult.getOrNull() ?: emptyList()
                    )
                } else {
                    val errorMsg = personalizedResult.exceptionOrNull()?.message 
                        ?: trendingResult.exceptionOrNull()?.message 
                        ?: "Failed to load recommendations"
                    _uiState.value = RecommendationState.Error(errorMsg)
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
                val result = repository.getCartSuggestions(userId, cartMeals)
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
}
