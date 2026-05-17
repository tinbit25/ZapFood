package com.example.food.domain.manager

import com.example.food.ui.viewmodel.CartViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VendorMenuObserver(
    private val cartViewModel: CartViewModel,
    private val syncEngine: VendorContentSyncEngine = VendorContentSyncEngine()
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val vendorObservations = mutableMapOf<String, Job>()

    fun startObservingVendors() {
        cartViewModel.cartState.onEach { state ->
            val vendorIds = state.meals.map { it.first.vendorId }.toSet()
            
            // Cancel observations for vendors no longer in cart
            val removedVendorIds = vendorObservations.keys.filter { it !in vendorIds }
            removedVendorIds.forEach { id ->
                vendorObservations[id]?.cancel()
                vendorObservations.remove(id)
            }

            // Start observing new vendors whose meals are in cart
            vendorIds.forEach { vendorId ->
                if (vendorId !in vendorObservations) {
                    val job = syncEngine.observeVendorStatus(vendorId)
                        .onEach { vendor ->
                            if (vendor != null && !vendor.isActive) {
                                val mealsToRemove = cartViewModel.cartState.value.meals
                                    .filter { it.first.vendorId == vendorId }
                                    .map { it.first.id }
                                
                                mealsToRemove.forEach { mealId ->
                                    cartViewModel.removeMeal(mealId)
                                }
                            }
                        }.launchIn(scope)
                    vendorObservations[vendorId] = job
                }
            }
        }.launchIn(scope)
    }

    fun stopObserving() {
        vendorObservations.values.forEach { it.cancel() }
        vendorObservations.clear()
    }
}
