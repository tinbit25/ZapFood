package com.example.food.data.model

import com.google.firebase.firestore.Exclude

enum class UserRole {
    CUSTOMER, VENDOR, ADMIN
}

enum class VendorStatus {
    PENDING, APPROVED, REJECTED, SUSPENDED
}

data class User(
    @get:Exclude val id: String = java.util.UUID.randomUUID().toString(), // Local-only, excluded from Firestore
    val userId: String = "", // Firebase UID — used for all Firestore identity
    val displayName: String? = null,
    val email: String = "",
    val phoneNumber: String = "", // Required for Chapa payments (format: 09xxxxxxxx)
    val photoUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER,
    val rewardPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = 0,
    val passwordHash: String = "",
    val activeSessionIds: List<String> = emptyList(),
    val isActive: Boolean = true,
    val fcmToken: String? = null,
    val bio: String? = null,
    val gender: String? = null,

    // Role-specific extensions
    val preferences: List<String> = emptyList(), // For Customer
    val dietaryNeeds: List<String> = emptyList(), // For Customer
    val cuisineType: String = "", // For Vendor
    val businessAddress: String = "", // For Vendor
    val vendorStatus: VendorStatus = VendorStatus.PENDING // For Vendor lifecycle
)
