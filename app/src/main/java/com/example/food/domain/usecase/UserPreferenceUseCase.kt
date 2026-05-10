package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.UserFoodPreference
import com.example.food.data.repository.UserPreferenceRepository

class UserPreferenceUseCase(
    private val repository: UserPreferenceRepository = UserPreferenceRepository()
) {
    fun observePreferences(userId: String) = repository.observePreferences(userId)

    suspend fun saveExplicitPreferences(prefs: UserFoodPreference): Resource<Unit> {
        return repository.updatePreferences(prefs)
    }

    suspend fun updateProfileAfterOrder(order: Order) {
        val prefsResult = repository.getPreferences(order.customerId)
        if (prefsResult is Resource.Success) {
            val currentPrefs = prefsResult.data ?: return
            val updatedPrefs = EthiopianBehaviorIntelligence.calculateUpdatedPreferences(currentPrefs, order)
            repository.updatePreferences(updatedPrefs)
        }
    }
}
