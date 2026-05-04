package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.MealPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealPlanViewModel(
    private val mealPlanUseCase: MealPlanUseCase = MealPlanUseCase()
) : ViewModel() {

    private val _myPlansState = MutableStateFlow<Resource<List<MealPlan>>>(Resource.Loading())
    val myPlansState: StateFlow<Resource<List<MealPlan>>> = _myPlansState.asStateFlow()

    private val _discoverPlansState = MutableStateFlow<Resource<List<MealPlan>>>(Resource.Loading())
    val discoverPlansState: StateFlow<Resource<List<MealPlan>>> = _discoverPlansState.asStateFlow()

    private val _currentPlan = MutableStateFlow<MealPlan?>(null)
    val currentPlan: StateFlow<MealPlan?> = _currentPlan.asStateFlow()

    fun fetchMyPlans(userId: String) {
        viewModelScope.launch {
            mealPlanUseCase.getMyPlans(userId).collect {
                _myPlansState.value = it
            }
        }
    }

    fun fetchDiscoverPlans() {
        viewModelScope.launch {
            mealPlanUseCase.getDiscoverPlans().collect {
                _discoverPlansState.value = it
            }
        }
    }

    fun selectPlan(plan: MealPlan) {
        _currentPlan.value = plan
    }

    fun addMealToCurrentPlan(day: Day, mealId: String) {
        val plan = _currentPlan.value ?: return
        viewModelScope.launch {
            val result = mealPlanUseCase.addMealToDay(plan, day, mealId)
            if (result is Resource.Success) {
                _currentPlan.value = result.data
            }
        }
    }

    fun removeMealFromCurrentPlan(day: Day, mealId: String) {
        val plan = _currentPlan.value ?: return
        viewModelScope.launch {
            val result = mealPlanUseCase.removeMealFromDay(plan, day, mealId)
            if (result is Resource.Success) {
                _currentPlan.value = result.data
            }
        }
    }

    fun savePlan(user: User, onResult: (Resource<Unit>) -> Unit) {
        val plan = _currentPlan.value ?: return
        viewModelScope.launch {
            val result = mealPlanUseCase.createMealPlan(user, plan)
            onResult(result)
        }
    }

    fun clonePlan(user: User, originalPlan: MealPlan, onResult: (Resource<MealPlan>) -> Unit) {
        viewModelScope.launch {
            val result = mealPlanUseCase.cloneMealPlan(user, originalPlan)
            onResult(result)
        }
    }
}
