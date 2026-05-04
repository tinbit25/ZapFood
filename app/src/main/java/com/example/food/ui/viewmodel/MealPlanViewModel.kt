package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.MealPlan
import com.example.food.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val mealRepository: MealRepository = MealRepository()
) : ViewModel() {

    private val _mealPlans = MutableStateFlow<List<MealPlan>>(emptyList())
    val mealPlans: StateFlow<List<MealPlan>> = _mealPlans.asStateFlow()

    init {
        fetchMealPlans()
    }

    private fun fetchMealPlans() {
        viewModelScope.launch {
            mealRepository.getMealPlans().collect { plans ->
                _mealPlans.value = plans
            }
        }
    }
}
