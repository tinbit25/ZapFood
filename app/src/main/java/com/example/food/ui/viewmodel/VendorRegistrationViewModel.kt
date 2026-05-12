package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VendorRegistrationState {
    object Idle : VendorRegistrationState()
    object Loading : VendorRegistrationState()
    object Success : VendorRegistrationState()
    data class Error(val message: String) : VendorRegistrationState()
}

class VendorRegistrationViewModel(
    private val vendorRepository: VendorRepository = VendorRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<VendorRegistrationState>(VendorRegistrationState.Idle)
    val uiState: StateFlow<VendorRegistrationState> = _uiState.asStateFlow()

    // Form State
    var businessName = MutableStateFlow("")
    var businessType = MutableStateFlow(VendorType.RESTAURANT)
    var description = MutableStateFlow("")
    var phoneNumber = MutableStateFlow("")
    var deliveryRadius = MutableStateFlow(5.0)
    var cuisineTypes = MutableStateFlow(listOf<String>())
    
    // Legal/Verification
    var taxId = MutableStateFlow("")
    var bankInfo = MutableStateFlow("")
    var mobileMoney = MutableStateFlow("")

    fun register(userId: String) {
        if (businessName.value.isBlank() || phoneNumber.value.isBlank()) {
            _uiState.value = VendorRegistrationState.Error("Please fill in required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = VendorRegistrationState.Loading
            
            val vendor = Vendor(
                userId = userId,
                businessName = businessName.value,
                businessType = businessType.value,
                description = description.value,
                phoneNumber = phoneNumber.value,
                deliveryRadiusKm = deliveryRadius.value,
                cuisineTypes = cuisineTypes.value,
                verificationInfo = VendorVerificationInfo(
                    taxId = taxId.value,
                    bankAccountInfo = bankInfo.value,
                    mobileMoneyNumber = mobileMoney.value
                )
            )

            val success = vendorRepository.registerVendor(vendor)
            if (success) {
                _uiState.value = VendorRegistrationState.Success
            } else {
                _uiState.value = VendorRegistrationState.Error("Registration failed. Please try again.")
            }
        }
    }

    fun resetState() {
        _uiState.value = VendorRegistrationState.Idle
    }
}
