package com.example.food.data.model

/**
 * OrderTimeline — A structured view of the order journey.
 * Wraps the history and provides high-level context for the UI.
 */
data class OrderTimeline(
    val orderId: String,
    val history: List<OrderStatusHistory> = emptyList(),
    val currentStatus: OrderStatus = OrderStatus.PENDING,
    val estimatedDeliveryTime: Long? = null,
    val orderType: OrderType = OrderType.DELIVERY,
    val pickupQRCode: String = "",
    val pickupToken: String = ""
)
