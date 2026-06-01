package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.datastore.OnboardingDataStore
import com.example.food.data.model.OnboardingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val onboardingDataStore: OnboardingDataStore
) : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _onboardingItems = MutableStateFlow<List<OnboardingItem>>(emptyList())
    val onboardingItems: StateFlow<List<OnboardingItem>> = _onboardingItems.asStateFlow()

    init {
        loadOnboardingItems()
        viewModelScope.launch {
            onboardingDataStore.readOnboardingState().collectLatest { completed ->
                _onboardingCompleted.value = completed
            }
        }
    }

    private fun loadOnboardingItems() {
        _onboardingItems.value = listOf(
            OnboardingItem(
                title = "Discover Ethiopian Foods",
                description = "Experience authentic Injera, savory Tibs, traditional coffee, and delicious cultural meals from local restaurants.",
                type = "discover",
                tags = listOf("Injera", "Tibs", "Coffee", "Traditional")
            ),
            OnboardingItem(
                title = "Order Your Way",
                description = "Get your meals delivered hot to your doorstep, prepared for quick takeaway, or enjoy dining in at restaurants.",
                type = "order",
                tags = listOf("Delivery", "Takeaway", "Dine-In")
            ),
            OnboardingItem(
                title = "Fast & Personalized",
                description = "Get AI-powered smart recommendations, find nearby restaurants, and quickly reorders customized just for you.",
                type = "personalized",
                tags = listOf("Smart Recommendations", "Nearby", "Quick Reorder")
            )
        )
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingDataStore.saveOnboardingState(true)
        }
    }
}
