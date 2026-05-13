package com.example.food.domain.mapper

import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor

object MealVendorMapper {
    fun mapToBusinessIdentity(meal: Meal, vendor: Vendor): Meal {
        return meal.copy(
            vendorId = vendor.userId,
            businessName = vendor.businessName
        )
    }

    fun mapWithFallback(meal: Meal, vendorId: String, businessName: String): Meal {
        return meal.copy(
            vendorId = vendorId,
            businessName = businessName
        )
    }
}
