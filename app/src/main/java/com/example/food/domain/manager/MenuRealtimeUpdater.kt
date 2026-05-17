package com.example.food.domain.manager

import com.example.food.ui.viewmodel.CartViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MenuRealtimeUpdater(
    private val cartViewModel: CartViewModel,
    private val syncEngine: VendorContentSyncEngine = VendorContentSyncEngine()
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val mealObservations = mutableMapOf<String, Job>()

    fun startTrackingCartAvailability() {
        cartViewModel.cartState.onEach { state ->
            val cartMealIds = state.meals.map { it.first.id }.toSet()
            
            // Cancel observations for meals no longer in cart
            val removedMealIds = mealObservations.keys.filter { it !in cartMealIds }
            removedMealIds.forEach { id ->
                mealObservations[id]?.cancel()
                mealObservations.remove(id)
            }

            // Start observing new meals added to cart
            state.meals.forEach { pair ->
                val meal = pair.first
                if (meal.id !in mealObservations) {
                    val job = syncEngine.observeMealAvailability(meal.id)
                        .onEach { updatedMeal ->
                            if (updatedMeal == null || !updatedMeal.isAvailable) {
                                cartViewModel.removeMeal(meal.id)
                            }
                        }.launchIn(scope)
                    mealObservations[meal.id] = job
                }
            }
        }.launchIn(scope)
    }

    fun stopTracking() {
        mealObservations.values.forEach { it.cancel() }
        mealObservations.clear()
    }
}
