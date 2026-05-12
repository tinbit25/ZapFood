package com.example.food.data.model

import java.util.UUID

enum class VendorType(val displayName: String) {
    RESTAURANT("Restaurant"),
    CAFE("Cafe"),
    BAKERY("Bakery"),
    GROCERY("Grocery"),
    CHEF_SERVICE("Chef Service")
}

enum class ServiceTag(val displayName: String, val icon: String) {
    DELIVERY("Delivery", "🚚"),
    DINE_IN("Dine-in", "🍽️"),
    TAKEAWAY("Takeaway", "🥡"),
    CHEF_AT_HOME("Chef at Home", "👨‍🍳")
}

enum class VerificationStatus {
    PENDING,
    REJECTED,
    VERIFIED
}

data class OperatingHours(
    val open: String = "08:00",
    val close: String = "22:00",
    val isClosed: Boolean = false
)

data class Vendor(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val businessName: String,
    // Hybrid Support: List of types instead of a single type
    val businessTypes: List<VendorType> = listOf(VendorType.RESTAURANT),
    val description: String,
    val cuisineTypes: List<String> = emptyList(),
    val operatingHours: Map<String, OperatingHours> = emptyMap(),
    val deliveryRadiusKm: Double = 5.0,
    val phoneNumber: String,
    val serviceTags: List<ServiceTag> = emptyList(),
    val logoUrl: String? = null,
    val rating: Float = 0f,
    val totalOrders: Int = 0,
    val isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val verificationInfo: VendorVerificationInfo? = null
)

data class VendorVerificationInfo(
    val businessLicenseUrl: String? = null,
    val taxId: String? = null,
    val sanitationCertificateUrl: String? = null,
    val bankAccountInfo: String? = null,
    val mobileMoneyNumber: String? = null
)
