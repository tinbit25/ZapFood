package com.example.food.core.qr

import com.google.gson.Gson
import java.util.UUID

data class SecureQRPayload(
    val orderId: String,
    val customerId: String,
    val vendorId: String,
    val pickupToken: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes validity
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): SecureQRPayload? {
            return try {
                Gson().fromJson(json, SecureQRPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

object SecureQRPayloadBuilder {
    fun buildFromOrder(
        orderId: String,
        customerId: String,
        vendorId: String,
        pickupToken: String
    ): SecureQRPayload {
        return SecureQRPayload(
            orderId = orderId,
            customerId = customerId,
            vendorId = vendorId,
            pickupToken = pickupToken
        )
    }
}
