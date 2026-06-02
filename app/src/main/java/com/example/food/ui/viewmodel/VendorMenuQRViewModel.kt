package com.example.food.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.Vendor
import com.example.food.data.repository.VendorRepository
import com.example.food.domain.manager.VendorQRGeneratorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VendorMenuQRUiState(
    val isLoading: Boolean = false,
    val qrBitmap: Bitmap? = null,
    val qrContent: String? = null,
    val error: String? = null,
    val vendor: Vendor? = null
)

class VendorMenuQRViewModel(
    private val vendorRepository: VendorRepository = VendorRepository(),
    private val qrGeneratorService: VendorQRGeneratorService = VendorQRGeneratorService()
) : ViewModel() {
    private val _uiState = MutableStateFlow(VendorMenuQRUiState())
    val uiState: StateFlow<VendorMenuQRUiState> = _uiState.asStateFlow()

    fun loadVendorAndGenerateQR(vendorId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val vendorResult = vendorRepository.getVendorById(vendorId)
            if (vendorResult == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Vendor not found"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(vendor = vendorResult)

            val qrBitmap: Bitmap? = qrGeneratorService.generateMenuQR(vendorResult)
            val qrContent: String = qrGeneratorService.generateMenuQRContent(vendorResult)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                qrBitmap = qrBitmap,
                qrContent = qrContent,
                error = if (qrBitmap == null) "Failed to generate QR code" else null
            )
        }
    }

    fun regenerateQR() {
        val vendor = _uiState.value.vendor
        if (vendor != null) {
            val qrBitmap: Bitmap? = qrGeneratorService.generateMenuQR(vendor)
            val qrContent: String = qrGeneratorService.generateMenuQRContent(vendor)
            _uiState.value = _uiState.value.copy(
                qrBitmap = qrBitmap,
                qrContent = qrContent,
                error = if (qrBitmap == null) "Failed to regenerate QR code" else null
            )
        }
    }
}
