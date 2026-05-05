package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.AIPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AIViewModel(
    private val aiPlanUseCase: AIPlanUseCase = AIPlanUseCase()
) : ViewModel() {

    private val _aiPlanState = MutableStateFlow<Resource<MealPlan>>(Resource.Loading())
    val aiPlanState: StateFlow<Resource<MealPlan>> = _aiPlanState.asStateFlow()

    private val _userPreferences = MutableStateFlow<AIPreference?>(null)
    val userPreferences: StateFlow<AIPreference?> = _userPreferences.asStateFlow()

    fun generatePlan(preferences: AIPreference) {
        _userPreferences.value = preferences
        viewModelScope.launch {
            _aiPlanState.value = Resource.Loading()
            val result = aiPlanUseCase.generateMealPlan(preferences)
            _aiPlanState.value = result
        }
    }
    
    fun updateGeneratedPlan(updatedPlan: MealPlan) {
        _aiPlanState.value = Resource.Success(updatedPlan)
    }
}
