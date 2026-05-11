package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.SpiceLevel
import com.example.food.data.model.UserFoodPreference
import com.example.food.domain.usecase.UserPreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PreferenceViewModel(
    private val useCase: UserPreferenceUseCase = UserPreferenceUseCase()
) : ViewModel() {

    private val _preferences = MutableStateFlow<Resource<UserFoodPreference>>(Resource.Loading())
    val preferences: StateFlow<Resource<UserFoodPreference>> = _preferences

    private val _onboardingStatus = MutableStateFlow<Resource<Unit>?>(null)
    val onboardingStatus: StateFlow<Resource<Unit>?> = _onboardingStatus

    fun loadPreferences(userId: String) {
        viewModelScope.launch {
            useCase.observePreferences(userId).collectLatest {
                _preferences.value = it
            }
        }
    }

    fun saveOnboardingPreferences(
        userId: String,
        spiceLevel: SpiceLevel,
        fastingMode: Boolean,
        favoriteMeals: List<String>,
        budgetRange: String
    ) {
        viewModelScope.launch {
            _onboardingStatus.value = Resource.Loading()
            val newPrefs = UserFoodPreference(
                userId = userId,
                spicePreference = spiceLevel,
                fastingMode = fastingMode,
                favoriteFoods = favoriteMeals,
                budgetPreference = budgetRange
            )
            val result = useCase.saveExplicitPreferences(newPrefs)
            _onboardingStatus.value = result
        }
    }
    
    fun resetOnboardingStatus() {
        _onboardingStatus.value = null
    }
}
