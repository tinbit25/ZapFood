package com.example.food.ui.viewmodel

import com.example.food.core.util.AnalyticsTracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.example.food.data.model.User
import com.example.food.domain.usecase.MealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class MealViewModel(
    private val mealUseCase: MealUseCase = MealUseCase()
) : ViewModel() {

    private val _mealsState = MutableStateFlow<Resource<List<Meal>>>(Resource.Loading())
    val mealsState: StateFlow<Resource<List<Meal>>> = _mealsState.asStateFlow()

    private val _currentFilters = MutableStateFlow(MealFilters())
    val currentFilters: StateFlow<MealFilters> = _currentFilters.asStateFlow()

    private var fetchJob: Job? = null

    init {
        fetchMeals()
    }

    fun fetchMeals() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            mealUseCase.getFilteredMeals(_currentFilters.value).collect {
                _mealsState.value = it
            }
        }
    }

    fun updateFilters(newFilters: MealFilters) {
        _currentFilters.value = newFilters
        fetchMeals()
    }

    fun searchMeals(query: String) {
        updateFilters(_currentFilters.value.copy(query = query))
    }

    fun applyCategory(category: com.example.food.data.model.EthiopianFoodCategory?, userId: String? = null) {
        updateFilters(_currentFilters.value.copy(category = category))
        userId?.let { uid ->
            // Log category view
        }
    }

    fun createMeal(user: User, meal: Meal, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.createMeal(user, meal)
            onResult(result)
        }
    }

    fun seedMeals(vendorIds: List<String>, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.seedMeals(vendorIds)
            onResult(result)
        }
    }

    fun seedMealsForVendor(user: com.example.food.data.model.User, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.seedMealsForVendor(user)
            onResult(result)
        }
    }

    fun deleteMeal(id: String, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.deleteMeal(id)
            onResult(result)
        }
    }

    fun updateMeal(meal: Meal, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.updateMeal(meal)
            onResult(result)
        }
    }
}

