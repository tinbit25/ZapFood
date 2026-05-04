package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.MealPlan
import com.example.food.domain.usecase.MealPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val mealPlanUseCase: MealPlanUseCase = MealPlanUseCase()
) : ViewModel() {

    private val _mealPlans = MutableStateFlow<List<MealPlan>>(emptyList())
    val mealPlans: StateFlow<List<MealPlan>> = _mealPlans.asStateFlow()

    init {
        fetchMealPlans()
    }

    private fun fetchMealPlans() {
        viewModelScope.launch {
            mealPlanUseCase.getMealPlans().collect { plans ->
                _mealPlans.value = plans
            }
        }
    }

    fun generateMPCode(plan: MealPlan): String {
        return mealPlanUseCase.generateMPCode(plan)
    }

    fun clonePlanForEditing(originalPlan: MealPlan, userId: String): MealPlan {
        return mealPlanUseCase.clonePlanForUser(originalPlan, userId)
    }
}
