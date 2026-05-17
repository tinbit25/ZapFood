package com.example.food.domain.manager

import com.example.food.data.model.OrderType

class CheckoutPricingManager {
    fun getPricingSummary(
        subtotal: Double,
        orderType: OrderType,
        pointsToRedeem: Int = 0
    ): CheckoutSummary {
        val deliveryFee = PricingEngine.calculateDeliveryFee(orderType)
        val discount = PricingEngine.calculateDiscount(pointsToRedeem)
        val tax = PricingEngine.calculateTax(subtotal)
        val total = PricingEngine.calculateTotal(subtotal, deliveryFee, discount)

        return CheckoutSummary(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            tax = tax,
            total = total
        )
    }
}

data class CheckoutSummary(
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val tax: Double,
    val total: Double
)
