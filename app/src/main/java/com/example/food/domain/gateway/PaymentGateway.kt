package com.example.food.domain.gateway

import com.example.food.data.model.PaymentMethod

interface PaymentGateway {
    suspend fun processPayment(amount: Double, method: PaymentMethod): GatewayResponse
    suspend fun verifyPayment(transactionRef: String): GatewayResponse
    suspend fun refundPayment(transactionRef: String): GatewayResponse
}

sealed class GatewayResponse {
    data class Success(val transactionRef: String) : GatewayResponse()
    data class Failure(val message: String) : GatewayResponse()
    object Processing : GatewayResponse()
}
