package com.example.food.data.model

import java.util.UUID

data class Customer(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID, // Reference to User.id
    val dietaryPreferences: List<String> = emptyList(),
    val healthGoals: List<String> = emptyList(),
    val addresses: List<String> = emptyList(),
    val favoriteVendors: List<UUID> = emptyList()
)
