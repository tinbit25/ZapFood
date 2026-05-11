package com.example.food.data.model

data class UserFoodPreference(
    val userId: String = "",
    val fastingMode: Boolean = false,
    val spicePreference: SpiceLevel = SpiceLevel.MEDIUM,
    val dietaryType: String = "ANY", // VEGAN, NON_VEGAN, ANY
    val budgetPreference: String = "STANDARD", // BUDGET, STANDARD, PREMIUM
    val favoriteFoods: List<String> = emptyList(),
    val preferredMealTime: String = "ANY",
    val favoriteVendors: List<String> = emptyList(),
    val favoriteCategories: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
