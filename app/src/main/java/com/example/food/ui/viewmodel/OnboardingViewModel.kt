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
                title = "Discover Ethiopian Meals",
                description = "Browse a wide selection of authentic Ethiopian dishes from your favorite local vendors and restaurants.",
                icon = "🥘"
            ),
            OnboardingItem(
                title = "Fast Delivery & Takeaway",
                description = "Get your favorite meals delivered straight to your door or ready for pickup in record time.",
                icon = "🛵"
            ),
            OnboardingItem(
                title = "Personalized Meal Plans",
                description = "Stay healthy and organized with our AI-powered meal planning tailored to your dietary needs and fasting habits.",
                icon = "📅"
            ),
            OnboardingItem(
                title = "Experience Ethiopian Culture",
                description = "Celebrate our rich culinary heritage with every bite. Discover hidden gems and traditional flavors.",
                icon = "🇪🇹"
            )
        )
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingDataStore.saveOnboardingState(true)
        }
    }
}
