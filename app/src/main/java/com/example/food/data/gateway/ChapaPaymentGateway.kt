package com.example.food.data.gateway

import android.util.Log
import com.example.food.data.api.PaymentBackendApi
import com.example.food.data.model.PaymentMethod
import com.example.food.domain.gateway.GatewayResponse
import com.example.food.domain.gateway.PaymentGateway

/**
 * ChapaPaymentGateway — Production implementation of PaymentGateway.
 *
 * Routes ALL payment operations through the FastAPI backend.
 * The Android app NEVER communicates with Chapa directly.
 *
 * Architecture: Android → PaymentBackendApi → FastAPI → Chapa API
 *
 * The gateway interface is adapted for redirect-based flows:
 * - [processPayment] is replaced by [initializePayment] which returns a checkout URL
 * - [verifyPayment] checks the final status after user returns from checkout
 */
class ChapaPaymentGateway(
    private val api: PaymentBackendApi = PaymentBackendApi()
) : PaymentGateway {

    companion object {
        private const val TAG = "ChapaPaymentGateway"
    }

    /**
     * Initialize a payment with the backend.
     *
     * This is the redirect-based flow entry point:
     * 1. Sends orderId + userId to the backend
     * 2. Backend creates a Chapa transaction
     * 3. Returns a GatewayResponse with the checkout URL as the transactionRef
     *
     * NOTE: The [amount] and [method] parameters are included for interface
     * compatibility but the actual amount is read from Firestore on the backend
     * to prevent client-side tampering.
     */
    override suspend fun processPayment(
        amount: Double,
        method: PaymentMethod
    ): GatewayResponse {
        // processPayment needs orderId/userId but the interface doesn't have them.
        // This method is not used directly — use initializePayment() instead.
        return GatewayResponse.Failure(
            "Use ChapaPaymentGateway.initializePayment(orderId, userId) instead"
        )
    }

    /**
     * Initialize a Chapa payment for an order.
     *
     * @param orderId The Firestore order ID
     * @param userId The Firebase user UID
     * @return ChapaInitResponse with checkout URL on success
     */
    suspend fun initializePayment(
        orderId: String,
        userId: String
    ): ChapaInitResponse {
        Log.i(TAG, "Initializing payment: order=$orderId, user=$userId")

        val result = api.initializePayment(orderId, userId)

        return result.fold(
            onSuccess = { data ->
                Log.i(TAG, "Payment initialized: txRef=${data.txRef}, url=${data.checkoutUrl}")
                ChapaInitResponse(
                    success = true,
                    checkoutUrl = data.checkoutUrl,
                    txRef = data.txRef,
                    paymentId = data.paymentId
                )
            },
            onFailure = { error ->
                Log.e(TAG, "Payment init failed: ${error.message}")
                ChapaInitResponse(
                    success = false,
                    errorMessage = error.message ?: "Payment initialization failed"
                )
            }
        )
    }

    /**
     * Verify a payment transaction with the backend.
     *
     * Called after the user returns from the Chapa checkout page.
     * The backend re-verifies with Chapa API and syncs Firestore.
     */
    override suspend fun verifyPayment(transactionRef: String): GatewayResponse {
        Log.i(TAG, "Verifying payment: txRef=$transactionRef")

        val result = api.verifyPayment(transactionRef)

        return result.fold(
            onSuccess = { data ->
                Log.i(TAG, "Verify result: status=${data.status}, amount=${data.amount}")
                when (data.status.uppercase()) {
                    "SUCCESS" -> GatewayResponse.Success(transactionRef)
                    "PROCESSING", "INITIATED" -> GatewayResponse.Processing
                    else -> GatewayResponse.Failure("Payment ${data.status}")
                }
            },
            onFailure = { error ->
                Log.e(TAG, "Verify failed: ${error.message}")
                GatewayResponse.Failure(error.message ?: "Verification failed")
            }
        )
    }

    /**
     * Refund is not supported via the client — handled server-side.
     */
    override suspend fun refundPayment(transactionRef: String): GatewayResponse {
        return GatewayResponse.Failure("Refunds are handled server-side. Contact support.")
    }
}

/**
 * Response from a Chapa payment initialization.
 */
data class ChapaInitResponse(
    val success: Boolean,
    val checkoutUrl: String = "",
    val txRef: String = "",
    val paymentId: String = "",
    val errorMessage: String = ""
)
