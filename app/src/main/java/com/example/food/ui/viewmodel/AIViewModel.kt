package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.domain.model.AIRecommendationResponse
import com.example.food.domain.usecase.GetAIRecommendationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AiRecommendationUiState {
    object Initial : AiRecommendationUiState()
    object Loading : AiRecommendationUiState()
    data class Success(val data: AIRecommendationResponse) : AiRecommendationUiState()
    data class Error(val message: String) : AiRecommendationUiState()
    object Empty : AiRecommendationUiState()
}

class AiViewModel(
    private val getRecommendationsUseCase: GetAIRecommendationsUseCase = GetAIRecommendationsUseCase(),
    private val aiPlanUseCase: com.example.food.domain.usecase.AIPlanUseCase = com.example.food.domain.usecase.AIPlanUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiRecommendationUiState>(AiRecommendationUiState.Initial)
    val uiState: StateFlow<AiRecommendationUiState> = _uiState.asStateFlow()

    private val _aiPlanState = MutableStateFlow<com.example.food.core.util.Resource<com.example.food.data.model.MealPlan>>(com.example.food.core.util.Resource.Loading())
    val aiPlanState: StateFlow<com.example.food.core.util.Resource<com.example.food.data.model.MealPlan>> = _aiPlanState.asStateFlow()

    fun fetchRecommendations(userId: String) {
        viewModelScope.launch {
            _uiState.value = AiRecommendationUiState.Loading
            val result = getRecommendationsUseCase(userId)
            
            result.onSuccess { response ->
                if (response.recommendations.isEmpty()) {
                    _uiState.value = AiRecommendationUiState.Empty
                } else {
                    _uiState.value = AiRecommendationUiState.Success(response)
                }
            }.onFailure { error ->
                _uiState.value = AiRecommendationUiState.Error(error.message ?: "Failed to load recommendations")
            }
        }
    }

    fun generatePlan(preferences: com.example.food.data.model.AIPreference) {
        viewModelScope.launch {
            _aiPlanState.value = com.example.food.core.util.Resource.Loading()
            val result = aiPlanUseCase.generateMealPlan(preferences)
            _aiPlanState.value = result
        }
    }
}
