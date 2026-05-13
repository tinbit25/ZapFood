package com.example.food.domain.util

import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.data.model.VerificationStatus
import com.example.food.data.repository.VendorRepository
import java.util.UUID

class VendorDataSeeder(
    private val vendorRepository: VendorRepository = VendorRepository()
) {
    suspend fun seedEthiopianVendors() {
        val vendors = listOf(
            createVendor(
                name = "Yod Abyssinia",
                type = VendorType.TRADITIONAL_ETHIOPIAN,
                rating = 4.9,
                deliveryFee = 0.0,
                time = 25..40,
                cover = "https://images.unsplash.com/photo-1541518763669-27fef04b14ea?w=800"
            ),
            createVendor(
                name = "Kategna Ethiopian",
                type = VendorType.TRADITIONAL_ETHIOPIAN,
                rating = 4.8,
                deliveryFee = 2.5,
                time = 30..45,
                cover = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=800"
            ),
            createVendor(
                name = "Tomoca Coffee",
                type = VendorType.CAFE,
                rating = 4.9,
                deliveryFee = 1.0,
                time = 10..20,
                cover = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800"
            ),
            createVendor(
                name = "Kaldis Coffee",
                type = VendorType.CAFE,
                rating = 4.6,
                deliveryFee = 1.5,
                time = 15..25,
                cover = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800"
            ),
            createVendor(
                name = "Mamas Kitchen",
                type = VendorType.RESTAURANT,
                rating = 4.7,
                deliveryFee = 0.0,
                time = 35..50,
                cover = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"
            ),
            createVendor(
                name = "Best Western Pastry",
                type = VendorType.BAKERY,
                rating = 4.5,
                deliveryFee = 2.0,
                time = 20..35,
                cover = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=800"
            ),
            createVendor(
                name = "Habesha Juice Bar",
                type = VendorType.JUICE_SHOP,
                rating = 4.4,
                deliveryFee = 1.0,
                time = 10..15,
                cover = "https://images.unsplash.com/photo-1622597467822-4780653d9943?w=800"
            ),
            createVendor(
                name = "Burger Queen",
                type = VendorType.FAST_FOOD,
                rating = 4.2,
                deliveryFee = 3.0,
                time = 15..25,
                cover = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800"
            )
        )

        vendors.forEach { vendorRepository.registerVendor(it) }
    }

    private fun createVendor(
        name: String,
        type: VendorType,
        rating: Double,
        deliveryFee: Double,
        time: IntRange,
        cover: String
    ): Vendor {
        val id = UUID.randomUUID().toString()
        return Vendor(
            userId = id,
            businessName = name,
            businessTypes = listOf(type),
            rating = rating.toFloat(),
            deliveryFee = deliveryFee,
            deliveryTimeMin = time.first,
            deliveryTimeMax = time.last,
            coverImageUrl = cover,
            logoUrl = "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&background=random",
            verificationStatus = VerificationStatus.APPROVED,
            profileCompleted = true,
            isActive = true
        )
    }
}
