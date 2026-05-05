package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")

    suspend fun saveOrder(order: Order): Resource<Unit> {
        return try {
            ordersCollection.document(order.orderId).set(order).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save order")
        }
    }

    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val doc = ordersCollection.document(orderId).get().await()
            doc.toObject(Order::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getOrdersForUser(userId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ordersCollection
            .whereEqualTo("customerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(Resource.Success(orders))
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersForVendor(vendorId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ordersCollection
            .whereEqualTo("vendorId", vendorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(Resource.Success(orders))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Resource<Unit> {
        return try {
            ordersCollection.document(orderId).update(
                "status", status,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update order status")
        }
    }

    suspend fun updatePaymentStatus(orderId: String, status: com.example.food.data.model.PaymentStatus): Resource<Unit> {
        return try {
            ordersCollection.document(orderId).update(
                "paymentStatus", status,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update payment status")
        }
    }
}
