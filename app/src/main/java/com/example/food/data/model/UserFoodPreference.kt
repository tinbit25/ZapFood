package com.example.food.data.model

data class UserFoodPreference(
    val userId: String = "",

    // Explicit Preferences
    val fastingMode: Boolean = false,
    val spiceTolerance: SpiceLevel = SpiceLevel.MEDIUM,
    val preferredBudgetRange: String = "Medium", // Low, Medium, High
    val dietaryPreferences: List<String> = emptyList(), // e.g., "vegan", "gluten-free"
    val explicitFavoriteMeals: List<String> = emptyList(), // Meal IDs
    val preferredMealTimes: List<String> = emptyList(), // e.g., "Breakfast", "Lunch"

    // Behavioral Tracking (Learned over time)
    val favoriteMeals: List<String> = emptyList(), // Meal IDs with highest order count
    val dislikedMeals: List<String> = emptyList(), // Explicitly disliked or low-rated
    
    val favoriteVendors: List<String> = emptyList(), // Vendor IDs with highest order count
    val frequentlyOrderedCategories: List<String> = emptyList(), // Deprecated in favor of favoriteCategories
    val favoriteCategories: List<String> = emptyList(),
    
    // Day/Time -> List of Categories or Meal IDs
    val mealTimePatterns: Map<String, List<String>> = emptyMap(), 
    
    val orderingPatterns: Map<String, Int> = emptyMap(), // e.g. "weekday" -> 5
    val cuisineAffinity: Map<String, Double> = emptyMap(), // e.g. "Amhara" -> 0.8
    val fastingBehavior: String = "Occasional", // Strict, Occasional, None
    
    // e.g., Map<VendorId, Count>
    val vendorInteractionCounts: Map<String, Int> = emptyMap(),
    // e.g., Map<MealId, Count>
    val mealOrderCounts: Map<String, Int> = emptyMap(),

    val lastUpdated: Long = System.currentTimeMillis()
)
