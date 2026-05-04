package com.example.food.data.model

import java.util.UUID

data class Admin(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String, // Reference to User.id
    val permissions: List<String> = emptyList(),
    val adminLevel: Int = 1
)
