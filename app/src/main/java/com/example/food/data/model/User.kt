package com.example.food.data.model

import java.util.UUID

enum class UserRole {
    CUSTOMER, VENDOR, ADMIN
}

data class User(
    val id: UUID = UUID.randomUUID(),
    val userId: String = "", // Firebase UID
    val displayName: String? = null,
    val email: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val rewardPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = 0,
    val passwordHash: String = "",
    val activeSessionIds: List<String> = emptyList(),
    
    // Role-specific extensions (Simplified)
    val preferences: List<String> = emptyList(), // For Customer
    val dietaryNeeds: List<String> = emptyList(), // For Customer
    val cuisineType: String = "", // For Vendor
    val businessAddress: String = "", // For Vendor
    val isApproved: Boolean = false // For Vendor/Admin control
)
