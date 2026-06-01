package com.example.food.domain.manager

import com.example.food.data.model.OrderType

class CheckoutPricingManager {
    fun getPricingSummary(
        subtotal: Double,
        orderType: OrderType,
        pointsToRedeem: Int = 0
    ): CheckoutSummary {
        val deliveryFee = PricingEngine.calculateDeliveryFee(orderType)
        val packagingFee = PricingEngine.calculatePackagingFee(orderType)
        val discount = PricingEngine.calculateDiscount(pointsToRedeem)
        val tax = PricingEngine.calculateTax(subtotal)
        val total = PricingEngine.calculateTotal(subtotal, deliveryFee, packagingFee, discount)

        return CheckoutSummary(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            packagingFee = packagingFee,
            discount = discount,
            tax = tax,
            total = total
        )
    }
}

data class CheckoutSummary(
    val subtotal: Double,
    val deliveryFee: Double,
    val packagingFee: Double,
    val discount: Double,
    val tax: Double,
    val total: Double
)
