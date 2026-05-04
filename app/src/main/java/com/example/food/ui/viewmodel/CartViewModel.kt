package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.food.data.model.Meal
import com.example.food.data.model.MealPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartState(
    val meals: List<Pair<Meal, Int>> = emptyList(),
    val mealPlans: List<Pair<MealPlan, Int>> = emptyList()
) {
    val subtotal: Double
        get() = (meals.sumOf { it.first.price * it.second }) + (mealPlans.sumOf { it.first.price * it.second })
}

class CartViewModel : ViewModel() {
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun addMeal(meal: Meal) {
        _cartState.update { state ->
            val existing = state.meals.find { it.first.id == meal.id }
            if (existing != null) {
                state.copy(meals = state.meals.map { 
                    if (it.first.id == meal.id) it.first to it.second + 1 else it 
                })
            } else {
                state.copy(meals = state.meals + (meal to 1))
            }
        }
    }

    fun addMealPlan(plan: MealPlan) {
        _cartState.update { state ->
            val existing = state.mealPlans.find { it.first.id == plan.id }
            if (existing != null) {
                state.copy(mealPlans = state.mealPlans.map { 
                    if (it.first.id == plan.id) it.first to it.second + 1 else it 
                })
            } else {
                state.copy(mealPlans = state.mealPlans + (plan to 1))
            }
        }
    }

    fun removeMeal(mealId: String) {
        _cartState.update { state ->
            state.copy(meals = state.meals.filter { it.first.id.toString() != mealId })
        }
    }

    fun removeMealPlan(planId: String) {
        _cartState.update { state ->
            state.copy(mealPlans = state.mealPlans.filter { it.first.id.toString() != planId })
        }
    }
    
    fun clearCart() {
        _cartState.value = CartState()
    }
}
