package com.example.food.domain.util

import com.example.food.data.model.User
import com.example.food.data.model.Vendor
import com.example.food.data.model.VerificationStatus

object VendorDataNormalizer {
    fun normalize(vendor: Vendor, user: User?): Vendor {
        return vendor.copy(
            businessName = if (vendor.businessName.isBlank()) {
                user?.displayName ?: "Unnamed Business"
            } else {
                vendor.businessName
            },
            verificationStatus = if (vendor.profileCompleted && vendor.verificationStatus == VerificationStatus.PENDING_REVIEW) {
                VerificationStatus.PENDING_REVIEW
            } else {
                vendor.verificationStatus
            }
        )
    }
}
