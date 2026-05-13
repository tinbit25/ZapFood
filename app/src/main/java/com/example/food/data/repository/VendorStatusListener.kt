package com.example.food.data.repository

import com.example.food.data.model.Vendor
import kotlinx.coroutines.flow.Flow

class VendorStatusListener(
    private val vendorRepository: VendorRepository = VendorRepository()
) {
    fun listenToStatusChanges(userId: String): Flow<Vendor?> {
        return vendorRepository.listenToVendorByUserId(userId)
    }
}
