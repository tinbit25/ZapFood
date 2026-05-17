package com.example.food.domain.manager

import com.example.food.data.model.OrderType

object PricingEngine {
    // VAT rate is strictly 0% per user instructions
    const val VAT_RATE = 0.0
    const val BASE_DELIVERY_FEE = 50.0
    const val BASE_DINE_IN_FEE = 0.0
    const val BASE_TAKEAWAY_FEE = 0.0

    // 10 points = 100 ETB discount
    const val POINTS_TO_DISCOUNT_RATIO = 10.0 

    fun calculateSubtotal(items: List<Pair<Double, Int>>): Double {
        return items.sumOf { it.first * it.second }
    }

    fun calculateDeliveryFee(orderType: OrderType): Double {
        return if (orderType == OrderType.DELIVERY) BASE_DELIVERY_FEE else 0.0
    }

    fun calculateDiscount(pointsToRedeem: Int): Double {
        return (pointsToRedeem / 10) * 100.0
    }

    fun calculateTax(subtotal: Double): Double {
        return subtotal * VAT_RATE
    }

    fun calculateTotal(subtotal: Double, deliveryFee: Double, discount: Double): Double {
        val calculatedTax = calculateTax(subtotal)
        return (subtotal + deliveryFee + calculatedTax - discount).coerceAtLeast(0.0)
    }
}
