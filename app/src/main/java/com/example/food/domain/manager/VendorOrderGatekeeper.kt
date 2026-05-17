package com.example.food.domain.manager

import com.example.food.data.model.OrderStatus
import com.google.firebase.firestore.Query

class VendorOrderGatekeeper {
    /**
     * Vendor dashboards must strictly only see orders where:
     * - Order state is SENT_TO_VENDOR or higher (ACCEPTED, PREPARING, etc.)
     * - Standard flow PENDING states (e.g. cash on delivery)
     */
    fun filterVendorOrdersQuery(query: Query): Query {
        return query.whereIn("orderStatus", listOf(
            OrderStatus.SENT_TO_VENDOR.name,
            OrderStatus.PENDING.name,
            OrderStatus.ACCEPTED.name,
            OrderStatus.PREPARING.name,
            OrderStatus.READY.name,
            OrderStatus.ON_THE_WAY.name,
            OrderStatus.DELIVERED.name
        ))
    }
}
