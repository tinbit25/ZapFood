package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.gateway.MockPaymentGateway
import com.example.food.data.model.Payment
import com.example.food.data.model.PaymentMethod
import com.example.food.data.model.PaymentStatus
import com.example.food.data.repository.OrderRepository
import com.example.food.data.repository.PaymentRepository
import com.example.food.domain.gateway.GatewayResponse
import com.example.food.domain.gateway.PaymentGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PaymentUseCase(
    private val paymentRepository: PaymentRepository = PaymentRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val paymentGateway: PaymentGateway = MockPaymentGateway()
) {

    fun initiatePayment(orderId: String, userId: String, amount: Double, method: PaymentMethod): Flow<Resource<Payment>> = flow {
        emit(Resource.Loading())

        // 1. Validate Order
        val order = orderRepository.getOrderById(orderId)
        if (order == null) {
            emit(Resource.Error("Order not found"))
            return@flow
        }

        if (order.customerId != userId) {
            emit(Resource.Error("Unauthorized: You do not own this order"))
            return@flow
        }

        // 2. Prevent Duplicate SUCCESSFUL Payments
        val existingPayment = paymentRepository.getPaymentByOrderId(orderId)
        if (existingPayment?.status == PaymentStatus.SUCCESS) {
            emit(Resource.Error("Order already paid"))
            return@flow
        }

        // 3. Create Payment Record
        val payment = Payment(
            orderId = orderId,
            userId = userId,
            amount = amount.toLong(),
            method = method,
            status = PaymentStatus.PROCESSING
        )
        paymentRepository.createPayment(payment)

        // 4. Call Gateway
        val response = paymentGateway.processPayment(amount, method)

        // 5. Handle Response
        when (response) {
            is GatewayResponse.Success -> {
                paymentRepository.updatePaymentStatus(payment.paymentId, PaymentStatus.SUCCESS, response.transactionRef)
                orderRepository.updatePaymentStatus(orderId, PaymentStatus.SUCCESS)
                emit(Resource.Success(payment.copy(status = PaymentStatus.SUCCESS, transactionRef = response.transactionRef)))
            }
            is GatewayResponse.Failure -> {
                paymentRepository.updatePaymentStatus(payment.paymentId, PaymentStatus.FAILED)
                orderRepository.updatePaymentStatus(orderId, PaymentStatus.FAILED)
                emit(Resource.Error(response.message))
            }
            GatewayResponse.Processing -> {
                emit(Resource.Loading())
            }
        }
    }

    suspend fun refundPayment(orderId: String): Resource<Unit> {
        val payment = paymentRepository.getPaymentByOrderId(orderId)
        if (payment == null || payment.status != PaymentStatus.SUCCESS || payment.transactionRef == null) {
            return Resource.Error("No successful payment found to refund")
        }

        val response = paymentGateway.refundPayment(payment.transactionRef)
        return when (response) {
            is GatewayResponse.Success -> {
                paymentRepository.updatePaymentStatus(payment.paymentId, PaymentStatus.REFUNDED)
                orderRepository.updatePaymentStatus(orderId, PaymentStatus.REFUNDED)
                Resource.Success(Unit)
            }
            is GatewayResponse.Failure -> Resource.Error(response.message)
            GatewayResponse.Processing -> Resource.Loading()
        }
    }
}
