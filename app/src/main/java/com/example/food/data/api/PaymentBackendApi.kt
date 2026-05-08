package com.example.food.data.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * PaymentBackendApi — HTTP client for communicating with the FastAPI backend.
 *
 * The Android app NEVER talks to Chapa directly.
 * All payment operations go through this API -> FastAPI Backend -> Chapa.
 */
class PaymentBackendApi(
    private val baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://cloning-silencer-army.ngrok-free.dev"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ── Data Models ─────────────────────────────────────────

    data class InitPaymentData(
        @SerializedName("checkout_url") val checkoutUrl: String = "",
        @SerializedName("tx_ref") val txRef: String = "",
        @SerializedName("payment_id") val paymentId: String = ""
    )

    data class VerifyPaymentData(
        val status: String = "",
        @SerializedName("tx_ref") val txRef: String = "",
        val amount: Double = 0.0,
        val currency: String = "ETB",
        @SerializedName("payment_id") val paymentId: String? = null,
        @SerializedName("order_id") val orderId: String? = null
    )

    data class HealthData(
        val status: String = "",
        val version: String = "",
        @SerializedName("firebase_connected") val firebaseConnected: Boolean = false,
        @SerializedName("chapa_configured") val chapaConfigured: Boolean = false,
        val currency: String = "",
        @SerializedName("debug_mode") val debugMode: Boolean = false
    )

    // ── API Methods ─────────────────────────────────────────

    suspend fun initializePayment(
        orderId: String,
        userId: String
    ): Result<InitPaymentData> = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(mapOf("order_id" to orderId, "user_id" to userId))
            val request = Request.Builder()
                .url("$baseUrl/api/payments/initialize")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()
            val response = client.newCall(request).execute()
            parseResponse(response.body?.string() ?: "", InitPaymentData::class.java)
        } catch (e: IOException) {
            Result.failure(PaymentApiException("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(PaymentApiException("Unexpected error: ${e.message}"))
        }
    }

    suspend fun verifyPayment(txRef: String): Result<VerifyPaymentData> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/api/payments/verify/$txRef")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                parseResponse(response.body?.string() ?: "", VerifyPaymentData::class.java)
            } catch (e: IOException) {
                Result.failure(PaymentApiException("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(PaymentApiException("Unexpected error: ${e.message}"))
            }
        }

    suspend fun healthCheck(): Result<HealthData> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/health")
                .get()
                .build()
            val response = client.newCall(request).execute()
            parseResponse(response.body?.string() ?: "", HealthData::class.java)
        } catch (e: IOException) {
            Result.failure(PaymentApiException("Backend unreachable: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(PaymentApiException("Unexpected error: ${e.message}"))
        }
    }

    // ── JSON Parser (avoids Gson generic erasure) ───────────

    private fun <T> parseResponse(json: String, dataClass: Class<T>): Result<T> {
        val root = gson.fromJson(json, JsonObject::class.java)
        val success = root.get("success")?.asBoolean ?: false
        val message = root.get("message")?.asString ?: "Unknown error"

        if (!success) return Result.failure(PaymentApiException(message))

        val dataElement = root.get("data")
        if (dataElement == null || dataElement.isJsonNull) {
            return Result.failure(PaymentApiException(message))
        }

        return Result.success(gson.fromJson(dataElement, dataClass))
    }
}

class PaymentApiException(message: String) : Exception(message)
