package com.example.food.core.qr

import com.google.gson.Gson

/**
 * Secure payload for vendor menu QR codes.
 * Customers scan this QR to view the vendor's menu.
 */
data class VendorMenuQRPayload(
    val vendorId: String,
    val businessName: String,
    val generatedAt: Long = System.currentTimeMillis()
) {
    fun toJson(): String = Gson().toJson(this)
    
    companion object {
        fun fromJson(json: String): VendorMenuQRPayload? {
            return try {
                Gson().fromJson(json, VendorMenuQRPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
