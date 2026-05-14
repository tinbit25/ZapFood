package com.example.food.domain.util

import java.security.SecureRandom

object PickupTokenGenerator {

    private val random = SecureRandom()
    private val CHAR_POOL: List<Char> = ('A'..'Z') + ('0'..'9')

    /**
     * Generates a 6-character alphanumeric pickup token.
     */
    fun generateToken(): String {
        return (1..6)
            .map { random.nextInt(CHAR_POOL.size) }
            .map(CHAR_POOL::get)
            .joinToString("")
    }

    /**
     * Generates the payload to be encoded into the QR code.
     */
    fun generateQRPayload(orderId: String, customerId: String, vendorId: String, token: String): String {
        return com.example.food.core.qr.SecureQRPayload(
            orderId = orderId,
            customerId = customerId,
            vendorId = vendorId,
            pickupToken = token,
            expiresAt = calculateExpiration()
        ).toJson()
    }

    /**
     * Calculates the expiration time for the QR code (5 minutes).
     */
    fun calculateExpiration(): Long {
        return System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes
    }
}
