package com.example.food.domain.manager

import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.google.firebase.firestore.Query

class VendorOrderGatekeeper {
    /**
     * Legacy query-based filter (kept for backwards compatibility/reference).
     */
    fun filterVendorOrdersQuery(query: Query): Query {
        return query.whereIn("orderStatus", listOf(
            OrderStatus.SENT_TO_VENDOR.name,
            OrderStatus.PENDING.name,
            OrderStatus.ACCEPTED.name,
            OrderStatus.PREPARING.name,
            OrderStatus.READY.name,
            OrderStatus.ON_THE_WAY.name,
            OrderStatus.DELIVERED.name,
            OrderStatus.COMPLETED.name,
            OrderStatus.BOOKED.name,
            OrderStatus.ARRIVED.name
        ))
    }

    /**
     * Robust in-memory filter that does not require any composite Firestore indexes.
     */
    fun filterVendorOrders(orders: List<Order>): List<Order> {
        val allowedStatuses = setOf(
            OrderStatus.PENDING,
            OrderStatus.BOOKED,
            OrderStatus.SENT_TO_VENDOR,
            OrderStatus.ACCEPTED,
            OrderStatus.PREPARING,
            OrderStatus.READY,
            OrderStatus.ON_THE_WAY,
            OrderStatus.ARRIVED,
            OrderStatus.DELIVERED,
            OrderStatus.COMPLETED,
            OrderStatus.CANCELLED
        )
        return orders.filter { it.orderStatus in allowedStatuses }
    }
}
