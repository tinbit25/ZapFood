package com.example.food.domain.model

import com.example.food.data.model.EthiopianFoodCategory
import com.example.food.data.model.MealTime
import com.example.food.data.model.ProteinLevel
import com.example.food.data.model.SpiceLevel

/**
 * Placeholder data structures for future AI recommendation systems.
 * This file contains the architecture for the planned collaborative filtering
 * and content-based recommendation engine for Ethiopian cuisine.
 */

data class MealEmbedding(
    val mealId: String,
    val vector: FloatArray, // e.g. [0.2, 0.5, -0.1, 0.9] representing categorized embeddings
    val dominantCategory: EthiopianFoodCategory,
    val isFastingFriendly: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MealEmbedding
        return mealId == other.mealId && vector.contentEquals(other.vector)
    }

    override fun hashCode(): Int {
        var result = mealId.hashCode()
        result = 31 * result + vector.contentHashCode()
        return result
    }
}

data class UserPreferenceProfile(
    val userId: String,
    val preferredCategories: Map<EthiopianFoodCategory, Float>, // Category to weight
    val prefersFasting: Boolean,
    val spiceTolerance: SpiceLevel,
    val preferredMealTimes: List<MealTime>,
    val explicitDislikes: List<String> // Tags or Meal IDs
)

data class RecommendationScore(
    val mealId: String,
    val score: Float, // 0.0 to 1.0
    val reasoning: List<String> // e.g., ["Matches fasting preference", "High protein"]
)

data class CollaborativeFilterMatch(
    val userId1: String,
    val userId2: String,
    val similarityScore: Float // Cosine similarity
)
