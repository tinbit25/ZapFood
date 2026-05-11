package com.example.food.data.model

import java.util.UUID

enum class InteractionType {
    CLICK,
    PURCHASE,
    FAVORITE,
    REMOVE_FAVORITE,
    IGNORE,
    REORDER,
    CART_ADD,
    CART_REMOVE,
    COMBO_ACCEPT,
    COMBO_IGNORE,
    SEARCH_CLICK
}

data class AnalyticsEvent(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val mealId: String? = null,
    val vendorId: String? = null,
    val interactionType: InteractionType,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val deviceType: String = "android",
    val mealCategory: String? = null,
    val mealTags: List<String> = emptyList(),
    val fastingRelevant: Boolean? = null,
    val mealTime: String? = null, // e.g., "breakfast", "lunch"
    val metadata: Map<String, String> = emptyMap()
)
