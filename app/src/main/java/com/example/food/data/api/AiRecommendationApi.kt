package com.example.food.data.api

import com.example.food.domain.model.AIAnalyticsEventRequest
import com.example.food.domain.model.AIPersonalizedRequest
import com.example.food.domain.model.AIRecommendationResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiRecommendationApi(
    private val baseUrl: String = PaymentBackendApi.DEFAULT_BASE_URL
) {
    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ── Recommendations ─────────────────────────────────────────

    suspend fun getAIRecommendations(userId: String): Result<AIRecommendationResponse> =
        getRequest("/api/recommendations/ai/$userId", AIRecommendationResponse::class.java)

    // ── Similarity ──────────────────────────────────────────────

    suspend fun getSimilarMeals(mealId: String): Result<AIRecommendationResponse> =
        getRequest("/api/ai/meals/$mealId/similar", AIRecommendationResponse::class.java)

    // ── Combos ──────────────────────────────────────────────────

    suspend fun getCombos(mealId: String): Result<AIRecommendationResponse> =
        getRequest("/api/ai/combos/$mealId", AIRecommendationResponse::class.java)

    // ── Analytics ───────────────────────────────────────────────
    
    suspend fun trackEvent(request: AIAnalyticsEventRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val body = gson.toJson(request)
                val httpRequest = Request.Builder()
                    .url("$baseUrl/api/ai/analytics/event")
                    .post(body.toRequestBody(JSON_MEDIA_TYPE))
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Analytics failed: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Network error tracking event"))
            }
        }

    // ── Helpers ──────────────────────────────────────────────────

    private suspend fun <T, R> postRequest(endpoint: String, requestData: T, responseClass: Class<R>): Result<R> =
        withContext(Dispatchers.IO) {
            try {
                val body = gson.toJson(requestData)
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .post(body.toRequestBody(JSON_MEDIA_TYPE))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    Result.success(gson.fromJson(responseBody, responseClass))
                } else {
                    Result.failure(Exception("Backend error: ${response.code}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }
        
    private suspend fun <R> getRequest(endpoint: String, responseClass: Class<R>): Result<R> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .get()
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                
                if (response.isSuccessful) {
                    Result.success(gson.fromJson(responseBody, responseClass))
                } else {
                    Result.failure(Exception("Backend error: ${response.code}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}"))
            }
        }
}
