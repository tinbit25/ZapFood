package com.example.food.domain.manager

import com.example.food.data.model.Vendor
import com.example.food.data.repository.VendorRepository

class VendorOnboardingManager(
    private val vendorRepository: VendorRepository = VendorRepository()
) {
    /**
     * Submits the initial vendor registration.
     */
    suspend fun submitOnboarding(vendor: Vendor): Boolean {
        // Ensure profile is marked as completed
        val onboardingVendor = vendor.copy(profileCompleted = true)
        return vendorRepository.registerVendor(onboardingVendor)
    }

    /**
     * Checks if a user has already started or completed onboarding.
     */
    suspend fun checkOnboardingStatus(userId: String): Boolean {
        val vendor = vendorRepository.getVendorByUserId(userId)
        return vendor?.profileCompleted ?: false
    }
}
