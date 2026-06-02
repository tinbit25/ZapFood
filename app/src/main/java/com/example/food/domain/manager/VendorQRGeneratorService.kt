package com.example.food.domain.manager

import android.graphics.Bitmap
import com.example.food.core.qr.QRGeneratorService
import com.example.food.core.qr.VendorMenuQRPayload
import com.example.food.data.model.Vendor

/**
 * Service for generating QR codes for vendor menu access.
 * Vendors can generate QR codes that customers can scan to view their menu.
 */
class VendorQRGeneratorService(
    private val qrGeneratorService: QRGeneratorService = QRGeneratorService
) {
    /**
     * Generates a QR code for a vendor's menu.
     * Customers can scan this QR to view the vendor's menu.
     */
    fun generateMenuQR(vendor: Vendor): Bitmap? {
        val payload = VendorMenuQRPayload(
            vendorId = vendor.id,
            businessName = vendor.businessName
        )
        val qrContent = payload.toJson()
        return qrGeneratorService.generateQRCode(qrContent, 512)
    }
    
    /**
     * Generates the QR content (JSON string) for a vendor's menu.
     * This can be used for testing or alternative QR generation methods.
     */
    fun generateMenuQRContent(vendor: Vendor): String {
        val payload = VendorMenuQRPayload(
            vendorId = vendor.id,
            businessName = vendor.businessName
        )
        return payload.toJson()
    }
}
