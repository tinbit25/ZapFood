package com.example.food.data.model

import java.util.UUID

data class Admin(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID, // Reference to User.id
    val permissions: List<String> = emptyList(),
    val adminLevel: Int = 1
)
