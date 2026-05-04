package com.example.food.data.model

import java.util.UUID

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiryTimestamp: Long
)

data class AuthSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val userId: String,
    val deviceName: String = "Android Device",
    val loginTimestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long,
    val isActive: Boolean = true
)

data class ResetToken(
    val token: String = UUID.randomUUID().toString(),
    val userId: String,
    val expiryTimestamp: Long = System.currentTimeMillis() + (15 * 60 * 1000), // 15 mins
    val isUsed: Boolean = false
)
