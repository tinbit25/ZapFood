package com.example.food.data.model

import java.util.UUID

enum class UserRole {
    CUSTOMER, VENDOR, ADMIN
}

data class User(
    val id: UUID = UUID.randomUUID(),
    val firebaseId: String = "",
    val displayName: String? = null,
    val email: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val rewardPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
