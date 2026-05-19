package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.*
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    // Hybrid: Multiple types
    var businessTypes = MutableStateFlow(listOf(VendorType.RESTAURANT))
    var description = MutableStateFlow("")
    var phoneNumber = MutableStateFlow("")
    var deliveryRadius = MutableStateFlow(5.0)
    var cuisineTypes = MutableStateFlow(listOf<String>())
    
    // Service Tags
    var serviceTags = MutableStateFlow(listOf<ServiceTag>())
    
    // Legal/Verification
    var taxId = MutableStateFlow("")
    var bankInfo = MutableStateFlow("")
    var mobileMoney = MutableStateFlow("")
    var payoutAccountName = MutableStateFlow("")
    var licenseUri = MutableStateFlow<android.net.Uri?>(null)
    var sanitationUri = MutableStateFlow<android.net.Uri?>(null)
    var nationalIdUri = MutableStateFlow<android.net.Uri?>(null)

    fun toggleBusinessType(type: VendorType) {
        val current = businessTypes.value.toMutableList()
        if (current.contains(type)) {
            if (current.size > 1) current.remove(type)
        } else {
            current.add(type)
        }
        businessTypes.value = current
    }

    fun toggleServiceTag(tag: ServiceTag) {
        val current = serviceTags.value.toMutableList()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        serviceTags.value = current
    }

    private suspend fun uploadFile(userId: String, docType: String, uri: android.net.Uri): String? {
        return try {
            val ref = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                .child("vendors/$userId/verification/$docType.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            android.util.Log.e("VendorRegistrationViewModel", "Failed to upload $docType: ${e.message}")
            null
        }
    }

    fun register(userId: String) {
        if (businessName.value.isBlank() || phoneNumber.value.isBlank()) {
            _uiState.value = VendorRegistrationState.Error("Please fill in required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = VendorRegistrationState.Loading
            
            // Upload documents to Firebase Storage
            val licenseUrl = licenseUri.value?.let { uploadFile(userId, "business_license", it) }
            val sanitationUrl = sanitationUri.value?.let { uploadFile(userId, "sanitation_certificate", it) }
            val nationalIdUrl = nationalIdUri.value?.let { uploadFile(userId, "national_id", it) }

            val vendor = Vendor(
                id = userId,
                userId = userId,
                businessName = businessName.value,
                businessTypes = businessTypes.value,
                description = description.value,
                phoneNumber = phoneNumber.value,
                deliveryRadiusKm = deliveryRadius.value,
                cuisineTypes = cuisineTypes.value,
                serviceTags = serviceTags.value,
                profileCompleted = true,
                verificationStatus = VerificationStatus.PENDING_REVIEW,
                verificationInfo = VendorVerificationInfo(
                    taxId = taxId.value,
                    bankAccountInfo = bankInfo.value,
                    mobileMoneyNumber = mobileMoney.value,
                    payoutAccountName = payoutAccountName.value.ifBlank { businessName.value },
                    businessLicenseUrl = licenseUrl,
                    sanitationCertificateUrl = sanitationUrl,
                    nationalIdUrl = nationalIdUrl
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
