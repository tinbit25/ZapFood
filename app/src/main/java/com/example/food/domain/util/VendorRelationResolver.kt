package com.example.food.domain.util

import com.example.food.data.model.Vendor
import com.example.food.data.repository.VendorRepository

class VendorRelationResolver(
    private val vendorRepository: VendorRepository = VendorRepository()
) {
    suspend fun resolveVendor(userId: String): Vendor? {
        return vendorRepository.getVendorByUserId(userId)
    }

    suspend fun getActiveBusinessName(userId: String, fallbackName: String? = null): String {
        val vendor = resolveVendor(userId)
        return vendor?.businessName ?: fallbackName ?: "Unknown Vendor"
    }
}
