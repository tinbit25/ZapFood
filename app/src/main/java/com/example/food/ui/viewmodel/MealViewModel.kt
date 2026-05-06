package com.example.food.ui.viewmodel

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

class MealViewModel(
    private val mealUseCase: MealUseCase = MealUseCase()
) : ViewModel() {

    private val _mealsState = MutableStateFlow<Resource<List<Meal>>>(Resource.Loading())
    val mealsState: StateFlow<Resource<List<Meal>>> = _mealsState.asStateFlow()

    private val _currentFilters = MutableStateFlow(MealFilters())
    val currentFilters: StateFlow<MealFilters> = _currentFilters.asStateFlow()

    init {
        fetchMeals()
    }

    fun fetchMeals() {
        viewModelScope.launch {
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

    fun applyCategory(category: String?) {
        updateFilters(_currentFilters.value.copy(category = category))
    }

    fun createMeal(user: User, meal: Meal, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.createMeal(user, meal)
            onResult(result)
        }
    }

    fun seedMeals(onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = mealUseCase.seedMeals()
            if (result is Resource.Success) {
                fetchMeals()
            }
            onResult(result)
        }
    }
}
