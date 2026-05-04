package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.User
import com.example.food.domain.usecase.ProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val profileUseCase: ProfileUseCase = ProfileUseCase()
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun updateGeneralProfile(user: User) {
        viewModelScope.launch {
            profileUseCase.updateProfile(user).collect { resource ->
                handleResource(resource)
            }
        }
    }

    fun updateCustomerPreferences(userId: String, preferences: List<String>, dietaryNeeds: List<String>) {
        viewModelScope.launch {
            profileUseCase.updateCustomerPreferences(userId, preferences, dietaryNeeds).collect { resource ->
                handleResource(resource)
            }
        }
    }

    fun updateVendorInfo(userId: String, cuisineType: String, address: String) {
        viewModelScope.launch {
            profileUseCase.updateVendorBusinessInfo(userId, cuisineType, address).collect { resource ->
                handleResource(resource)
            }
        }
    }

    private fun handleResource(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Loading -> _profileState.value = ProfileState.Loading
            is Resource.Success -> _profileState.value = ProfileState.Success
            is Resource.Error -> _profileState.value = ProfileState.Error(resource.message ?: "Update failed")
        }
    }

    fun resetState() {
        _profileState.value = ProfileState.Idle
    }
}
