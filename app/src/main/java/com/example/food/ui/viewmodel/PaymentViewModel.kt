package com.example.food.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.api.PaymentBackendApi
import com.example.food.data.gateway.ChapaPaymentGateway
import com.example.food.data.gateway.ChapaInitResponse
import com.example.food.data.model.Payment
import com.example.food.data.model.PaymentMethod
import com.example.food.data.model.PaymentStatus
import com.example.food.domain.gateway.GatewayResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Payment states for the Chapa redirect-based flow.
 */
sealed class PaymentState {
    /** No payment in progress. */
    object Idle : PaymentState()

    /** Backend is initializing the Chapa transaction. */
    object Loading : PaymentState()

    /**
     * Chapa checkout URL ready — redirect user to this URL.
     * After user completes payment in browser, call verifyPayment().
     */
    data class CheckoutReady(
        val checkoutUrl: String,
        val txRef: String,
        val paymentId: String
    ) : PaymentState()

    /** Payment verified as successful. */
    data class Success(val payment: Payment) : PaymentState()

    /** Payment failed or was cancelled. */
    data class Error(val message: String) : PaymentState()

    /** User returned from checkout, verifying with backend... */
    object Verifying : PaymentState()
}

/**
 * PaymentViewModel — Orchestrates the Chapa payment flow.
 *
 * Flow:
 * 1. [initiatePayment] → backend creates Chapa transaction → CheckoutReady state
 * 2. UI opens checkout URL in Custom Tabs
 * 3. User returns → [verifyPayment] → backend checks Chapa → Success/Error state
 */
class PaymentViewModel(
    private val gateway: ChapaPaymentGateway = ChapaPaymentGateway()
) : ViewModel() {

    companion object {
        private const val TAG = "PaymentViewModel"
        private const val MAX_VERIFY_RETRIES = 5
        private const val VERIFY_RETRY_DELAY_MS = 2000L
    }

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    // Store current transaction for verification after user returns
    private var currentTxRef: String? = null
    private var currentOrderId: String? = null

    /**
     * Step 1: Initialize payment with the backend.
     *
     * On success, emits [PaymentState.CheckoutReady] with the Chapa checkout URL.
     * The UI should then open this URL in Custom Tabs.
     */
    fun initiatePayment(
        orderId: String,
        userId: String,
        amount: Double,
        method: PaymentMethod
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading
            currentOrderId = orderId
            Log.i(TAG, "Initiating payment: order=$orderId")

            try {
                val result: ChapaInitResponse = gateway.initializePayment(orderId, userId)

                if (result.success && result.checkoutUrl.isNotEmpty()) {
                    currentTxRef = result.txRef
                    _paymentState.value = PaymentState.CheckoutReady(
                        checkoutUrl = result.checkoutUrl,
                        txRef = result.txRef,
                        paymentId = result.paymentId
                    )
                    Log.i(TAG, "Checkout ready: txRef=${result.txRef}")
                } else {
                    _paymentState.value = PaymentState.Error(
                        result.errorMessage.ifEmpty { "Failed to initialize payment" }
                    )
                    Log.e(TAG, "Init failed: ${result.errorMessage}")
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(
                    e.message ?: "Unexpected error during payment initialization"
                )
                Log.e(TAG, "Init exception", e)
            }
        }
    }

    /**
     * Step 2: Verify payment after user returns from Chapa checkout.
     *
     * Called when the user comes back from the browser/Custom Tabs.
     * Retries verification up to [MAX_VERIFY_RETRIES] times with delay,
     * since Chapa may take a moment to process the payment.
     *
     * @param txRef Optional tx_ref override (uses stored one if null)
     */
    fun verifyPayment(txRef: String? = null) {
        val ref = txRef ?: currentTxRef
        if (ref.isNullOrEmpty()) {
            _paymentState.value = PaymentState.Error("No transaction to verify")
            return
        }

        viewModelScope.launch {
            _paymentState.value = PaymentState.Verifying
            Log.i(TAG, "Verifying payment: txRef=$ref")

            for (attempt in 1..MAX_VERIFY_RETRIES) {
                try {
                    val response = gateway.verifyPayment(ref)

                    when (response) {
                        is GatewayResponse.Success -> {
                            // Critical Sync: Update order status and generate QR Code before notifying UI
                            val orderId = currentOrderId ?: ""
                            if (orderId.isNotEmpty()) {
                                try {
                                    val repo = com.example.food.data.repository.OrderRepository()
                                    val syncManager = com.example.food.domain.manager.UnifiedOrderManager(
                                        repo, 
                                        com.example.food.domain.manager.OrderStateSynchronizer(repo)
                                    )
                                    syncManager.syncPaymentStatus(orderId, PaymentStatus.SUCCESS)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to sync order state after payment", e)
                                }
                            }

                            _paymentState.value = PaymentState.Success(
                                Payment(
                                    orderId = orderId,
                                    transactionRef = ref,
                                    status = PaymentStatus.SUCCESS
                                )
                            )
                            Log.i(TAG, "Payment verified and synchronized: SUCCESS")
                            return@launch
                        }
                        is GatewayResponse.Processing -> {
                            // Still processing — retry after delay
                            if (attempt < MAX_VERIFY_RETRIES) {
                                Log.i(TAG, "Still processing, retry $attempt/$MAX_VERIFY_RETRIES")
                                delay(VERIFY_RETRY_DELAY_MS)
                            }
                        }
                        is GatewayResponse.Failure -> {
                            _paymentState.value = PaymentState.Error(response.message)
                            Log.e(TAG, "Verification failed: ${response.message}")
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    if (attempt == MAX_VERIFY_RETRIES) {
                        _paymentState.value = PaymentState.Error(
                            "Verification failed: ${e.message}"
                        )
                        Log.e(TAG, "Verify exception on final attempt", e)
                        return@launch
                    }
                    delay(VERIFY_RETRY_DELAY_MS)
                }
            }

            // All retries exhausted, still processing
            _paymentState.value = PaymentState.Error(
                "Payment is still processing. Please check your order status later."
            )
        }
    }

    /**
     * Retry a failed payment from scratch.
     */
    fun retryPayment(orderId: String, userId: String, amount: Double, method: PaymentMethod) {
        resetState()
        initiatePayment(orderId, userId, amount, method)
    }

    /**
     * Reset to idle state.
     */
    fun resetState() {
        _paymentState.value = PaymentState.Idle
        currentTxRef = null
        currentOrderId = null
    }
}
