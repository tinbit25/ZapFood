package com.example.food.data.model

import java.util.UUID

data class Customer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String, // Reference to User.id
    val dietaryPreferences: List<String> = emptyList(),
    val healthGoals: List<String> = emptyList(),
    val addresses: List<String> = emptyList(),
    val favoriteVendors: List<String> = emptyList()
)
