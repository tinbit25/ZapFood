package com.example.food.ui.viewmodel

import com.example.food.core.util.AnalyticsTracker
import com.example.food.data.model.InteractionType

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
        get() {
            val mealPrices = meals.map { it.first.price to it.second }
            val planPrices = mealPlans.map { it.first.price to it.second }
            return com.example.food.domain.manager.PricingEngine.calculateSubtotal(mealPrices + planPrices)
        }
}

class CartViewModel : ViewModel() {
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun addMeal(meal: Meal, userId: String? = null) {
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
        userId?.let { uid ->
            AnalyticsTracker.logEvent(uid, InteractionType.CART_ADD, meal)
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
            state.copy(meals = state.meals.filter { it.first.id != mealId })
        }
    }

    fun removeMealPlan(planId: String) {
        _cartState.update { state ->
            state.copy(mealPlans = state.mealPlans.filter { it.first.id != planId })
        }
    }
    
    fun clearCart() {
        _cartState.value = CartState()
    }
}
