package com.example.food.data.gateway

import com.example.food.data.model.PaymentMethod
import com.example.food.domain.gateway.GatewayResponse
import com.example.food.domain.gateway.PaymentGateway
import kotlinx.coroutines.delay
import java.util.UUID

class MockPaymentGateway : PaymentGateway {
    
    override suspend fun processPayment(amount: Double, method: PaymentMethod): GatewayResponse {
        delay(2000) // Simulate network delay
        
        // Randomly simulate success or failure (80% success)
        return if (Math.random() < 0.8) {
            GatewayResponse.Success("TRANS_${UUID.randomUUID().toString().take(8).uppercase()}")
        } else {
            GatewayResponse.Failure("Insufficient funds or gateway timeout")
        }
    }

    override suspend fun verifyPayment(transactionRef: String): GatewayResponse {
        delay(1000)
        return GatewayResponse.Success(transactionRef)
    }

    override suspend fun refundPayment(transactionRef: String): GatewayResponse {
        delay(1500)
        return GatewayResponse.Success("REFUND_${transactionRef}")
    }
}
