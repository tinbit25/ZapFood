package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.model.Vendor
import com.example.food.data.model.VerificationStatus
import com.example.food.data.repository.VendorRealtimeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorUIState {
    object Loading : VendorUIState()
    object OnboardingRequired : VendorUIState()
    object PendingReview : VendorUIState()
    object Verifying : VendorUIState()
    object Active : VendorUIState()
    object Suspended : VendorUIState()
    object Rejected : VendorUIState()
    object NotAVendor : VendorUIState()
}

class VendorStateManager @Inject constructor(
    private val repository: VendorRealtimeRepository = VendorRealtimeRepository(),
    private val statusListener: com.example.food.data.repository.VendorStatusListener = com.example.food.data.repository.VendorStatusListener()
) : ViewModel() {

    private val _vendor = MutableStateFlow<Vendor?>(null)
    val vendor: StateFlow<Vendor?> = _vendor.asStateFlow()

    private val _uiState = MutableStateFlow<VendorUIState>(VendorUIState.Loading)
    val uiState: StateFlow<VendorUIState> = _uiState.asStateFlow()

    private var observationJob: kotlinx.coroutines.Job? = null

    fun startObserving(userId: String?) {
        if (userId == null || userId.isBlank()) {
            _uiState.value = VendorUIState.NotAVendor
            return
        }

        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            statusListener.listenToStatusChanges(userId).collect { vendor: com.example.food.data.model.Vendor? ->
                _vendor.value = vendor
                _uiState.value = mapVendorToUIState(vendor)
            }
        }
    }

    private fun mapVendorToUIState(vendor: Vendor?): VendorUIState {
        if (vendor == null) return VendorUIState.OnboardingRequired
        
        if (!vendor.profileCompleted) return VendorUIState.OnboardingRequired

        return when (vendor.verificationStatus) {
            VerificationStatus.PENDING_REVIEW, VerificationStatus.PENDING -> VendorUIState.PendingReview
            VerificationStatus.VERIFYING -> VendorUIState.Verifying
            VerificationStatus.ACTIVE, VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> VendorUIState.Active
            VerificationStatus.SUSPENDED -> VendorUIState.Suspended
            VerificationStatus.REJECTED -> VendorUIState.Rejected
        }
    }

    fun completeOnboarding(vendorId: String) {
        viewModelScope.launch {
            repository.completeOnboarding(vendorId)
        }
    }

    fun toggleActiveStatus(isActive: Boolean) {
        val currentVendor = _vendor.value ?: return
        viewModelScope.launch {
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("vendors")
                    .document(currentVendor.userId)
                    .update("isActive", isActive)
            } catch (e: Exception) {
                // Log or handle error
            }
        }
    }
}

