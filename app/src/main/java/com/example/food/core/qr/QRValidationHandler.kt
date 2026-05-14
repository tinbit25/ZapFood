package com.example.food.core.qr

import com.example.food.data.model.Order
import com.example.food.core.util.Resource

object QRValidationHandler {

    sealed class ValidationResult {
        data class Success(val payload: SecureQRPayload) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    /**
     * Validates a scanned QR string against the expected order context.
     */
    fun validateScannedQR(
        scannedData: String,
        currentOrder: Order
    ): ValidationResult {
        val payload = SecureQRPayload.fromJson(scannedData)
            ?: return ValidationResult.Error("Invalid QR Format. Please ask the customer to refresh their code.")

        // 1. Check if the QR belongs to this order
        if (payload.orderId != currentOrder.orderId) {
            return ValidationResult.Error("Order ID Mismatch. This QR belongs to another order.")
        }

        // 2. Check Expiration
        if (System.currentTimeMillis() > payload.expiresAt) {
            return ValidationResult.Error("QR Code Expired. Please ask the customer to refresh their code.")
        }

        // 3. Check Vendor ID
        if (payload.vendorId != currentOrder.vendorId) {
            return ValidationResult.Error("Vendor ID Mismatch. This order is not assigned to you.")
        }

        // 4. Token Match
        if (payload.pickupToken != currentOrder.pickupToken) {
            return ValidationResult.Error("Security Token Mismatch. Please refresh the order state.")
        }

        return ValidationResult.Success(payload)
    }
}
