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

    // MPCode logic
    fun generateMPCode(planId: String): String {
        val plan = _mealPlans.value.find { it.mealPlanId == planId }
        return plan?.mpcode ?: "MP-${planId.takeLast(4)}-${(1000..9999).random()}"
    }

    // Prototype Pattern: Clone plan for customization
    fun clonePlanForEditing(originalPlan: MealPlan, newOwnerId: String): MealPlan {
        return originalPlan.copy(
            mealPlanId = "custom-${originalPlan.mealPlanId}-${System.currentTimeMillis()}",
            mealPlanName = "Custom ${originalPlan.mealPlanName}",
            ownerId = newOwnerId,
            mpcode = "" // New plan, no code yet
        )
    }
}
