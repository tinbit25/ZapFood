package com.example.food.data.model

import java.util.UUID

data class Meal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val vendorId: String = "",
    val vendorName: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "General",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),

    // Ethiopian AI Metadata
    val cuisineType: CuisineType = CuisineType.ETHIOPIAN,
    val spiceLevel: SpiceLevel = SpiceLevel.MEDIUM,
    val fastingFriendly: Boolean = false,
    val veganFriendly: Boolean = false,

    val proteinLevel: ProteinLevel = ProteinLevel.MEDIUM,
    val carbLevel: CarbLevel = CarbLevel.MEDIUM,
    val oilLevel: OilLevel = OilLevel.MEDIUM,

    val mealTime: List<MealTime> = emptyList(),

    val popularityScore: Double = 0.0,
    val averageRating: Double = 0.0,

    val tags: List<String> = emptyList(),
    
    // Legacy support
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fats: Float = 0f,
    val rating: Float = 0f
) {
    fun isValid(): Boolean {
        if (name.isBlank() || price < 0) return false
        if (vendorId.isBlank()) return false
        if (tags.any { it.isBlank() }) return false // Empty tag prevention
        return true
    }
}

data class MealFilters(
    val category: EthiopianFoodCategory? = null,
    val vendorId: String? = null,
    val query: String? = null,
    
    // AI-Ready Metadata Filters
    val cuisineType: CuisineType? = null,
    val spiceLevel: SpiceLevel? = null,
    val fastingFriendly: Boolean? = null,
    val veganFriendly: Boolean? = null,
    val proteinLevel: ProteinLevel? = null,
    val carbLevel: CarbLevel? = null,
    val oilLevel: OilLevel? = null,
    val mealTime: MealTime? = null,
    val tag: String? = null,
    
    val minCalories: Int? = null,
    val maxCalories: Int? = null
)

