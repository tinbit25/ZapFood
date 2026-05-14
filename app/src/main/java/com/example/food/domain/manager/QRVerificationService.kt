package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderType
import com.example.food.data.repository.OrderRepository

/**
 * QRVerificationService
 *
 * Provides business logic to verify a takeaway order via its token or QR payload.
 */
class QRVerificationService(
    private val orderRepository: OrderRepository
) {
    /**
     * Verifies the given token against the order.
     * If valid, updates the order as picked up and delivered.
     */
    suspend fun verifyToken(order: Order, inputToken: String): Resource<Unit> {
        if (order.orderType != OrderType.TAKEAWAY) {
            return Resource.Error("Only takeaway orders require QR verification.")
        }
        if (order.pickupVerified) {
            return Resource.Error("This order has already been verified and picked up.")
        }
        
        if (order.paymentStatus != com.example.food.data.model.PaymentStatus.SUCCESS && order.paymentMethod != com.example.food.data.model.PaymentMethod.CASH) {
            return Resource.Error("Caution: Payment not yet confirmed by Chapa.")
        }

        if (System.currentTimeMillis() > order.qrExpiresAt) {
            return Resource.Error("This pickup QR code has expired.")
        }
        
        // Simple 6-character token or full payload verification
        val isMatch = inputToken.equals(order.pickupToken, ignoreCase = true) || 
                      inputToken == order.pickupQRCode
                      
        if (!isMatch) {
            return Resource.Error("Invalid token or QR code.")
        }

        return orderRepository.verifyQRPickup(order.orderId)
    }
}
