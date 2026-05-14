package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderType
import com.example.food.data.repository.OrderRepository
import com.example.food.domain.util.PickupTokenGenerator

/**
 * QRPickupManager
 *
 * Orchestrates the generation and assignment of a QR pickup token
 * for Takeaway orders.
 */
class QRPickupManager(
    private val orderRepository: OrderRepository
) {
    /**
     * Generates a new token and QR payload, and saves them to the order.
     */
    suspend fun assignQRToOrder(order: Order): Resource<Unit> {
        if (order.orderType != OrderType.TAKEAWAY) {
            return Resource.Error("QR codes are only generated for Takeaway orders.")
        }
        
        if (order.pickupQRCode.isNotBlank()) {
            // Already generated
            return Resource.Success(Unit)
        }

        val token = PickupTokenGenerator.generateToken()
        val qrCodePayload = PickupTokenGenerator.generateQRPayload(order.orderId, token)
        val expiresAt = PickupTokenGenerator.calculateExpiration()

        return orderRepository.updateQRPickupFields(
            orderId = order.orderId,
            token = token,
            qrCode = qrCodePayload,
            expiresAt = expiresAt
        )
    }
}
