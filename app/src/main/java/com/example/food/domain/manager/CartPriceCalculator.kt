package com.example.food.domain.manager

import com.example.food.data.model.Meal
import com.example.food.data.model.OrderType

class CartPriceCalculator {
    fun calculateCartSummary(
        cartItems: List<Pair<Meal, Int>>,
        orderType: OrderType,
        pointsToRedeem: Int = 0
    ): CartSummary {
        val itemsMap = cartItems.map { it.first.price to it.second }
        val subtotal = PricingEngine.calculateSubtotal(itemsMap)
        val deliveryFee = PricingEngine.calculateDeliveryFee(orderType)
        val discount = PricingEngine.calculateDiscount(pointsToRedeem)
        val tax = PricingEngine.calculateTax(subtotal)
        val total = PricingEngine.calculateTotal(subtotal, deliveryFee, discount)

        return CartSummary(
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            tax = tax,
            total = total
        )
    }
}

data class CartSummary(
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val tax: Double,
    val total: Double
)
