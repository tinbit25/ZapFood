package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.qr.VendorMenuQRPayload
import com.example.food.data.model.Vendor
import com.example.food.data.repository.VendorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VendorMenuQRScannerUiState(
    val isLoading: Boolean = false,
    val vendor: Vendor? = null,
    val error: String? = null,
    val scanSuccess: Boolean = false
)

class VendorMenuQRScannerViewModel(
    private val vendorRepository: VendorRepository = VendorRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(VendorMenuQRScannerUiState())
    val uiState: StateFlow<VendorMenuQRScannerUiState> = _uiState.asStateFlow()

    fun parseQRCode(qrContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val payload = VendorMenuQRPayload.fromJson(qrContent)
            if (payload == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Invalid QR code format"
                )
                return@launch
            }

            val vendor = vendorRepository.getVendorById(payload.vendorId)
            if (vendor == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Vendor not found"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                vendor = vendor,
                scanSuccess = true,
                error = null
            )
        }
    }

    fun resetState() {
        _uiState.value = VendorMenuQRScannerUiState()
    }
}
