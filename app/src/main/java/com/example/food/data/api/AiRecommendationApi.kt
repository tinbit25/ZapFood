package com.example.food.data.api

import com.example.food.domain.model.ComboRecommendation
import com.example.food.domain.model.ComboRequest
import com.example.food.domain.model.RecommendationRequest
import com.example.food.domain.model.RecommendationResponse
import com.example.food.domain.model.SimilarityRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.google.gson.reflect.TypeToken

class AiRecommendationApi(
    private val baseUrl: String = PaymentBackendApi.DEFAULT_BASE_URL // Assuming same FastAPI backend
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

    suspend fun getPersonalized(request: RecommendationRequest): Result<RecommendationResponse> =
        postRequest("/api/recommendations/personalized", request, RecommendationResponse::class.java)

    suspend fun getTrending(request: RecommendationRequest): Result<RecommendationResponse> =
        postRequest("/api/recommendations/trending", request, RecommendationResponse::class.java)

    suspend fun getFasting(request: RecommendationRequest): Result<RecommendationResponse> =
        postRequest("/api/recommendations/fasting", request, RecommendationResponse::class.java)

    // ── Similarity ──────────────────────────────────────────────

    suspend fun getSimilarMeals(mealId: String, request: SimilarityRequest): Result<RecommendationResponse> =
        postRequest("/api/similarity/meals/$mealId/similar", request, RecommendationResponse::class.java)

    // ── Combos ──────────────────────────────────────────────────

    suspend fun getCartSuggestions(request: ComboRequest): Result<List<ComboRecommendation>> =
        withContext(Dispatchers.IO) {
            try {
                val body = gson.toJson(request)
                val httpRequest = Request.Builder()
                    .url("$baseUrl/api/combos/cart-suggestions")
                    .post(body.toRequestBody(JSON_MEDIA_TYPE))
                    .build()
                val response = client.newCall(httpRequest).execute()
                
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val listType = object : TypeToken<List<ComboRecommendation>>() {}.type
                    val list: List<ComboRecommendation> = gson.fromJson(responseBody, listType)
                    Result.success(list)
                } else {
                    Result.failure(Exception("Backend error: ${response.code}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── Helper ──────────────────────────────────────────────────

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
}
