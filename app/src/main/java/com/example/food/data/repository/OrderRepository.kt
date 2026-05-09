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

import com.example.food.data.model.DeliveryStatus
import com.example.food.data.model.OrderStatusHistory
import com.example.food.data.model.OrderTimeline
import com.google.firebase.firestore.FieldValue

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

    fun observeOrderTimeline(orderId: String): Flow<Resource<OrderTimeline>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Timeline fetch failed"))
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)
                if (order != null) {
                    android.util.Log.d("ORDER_SYNC", "Realtime update received for timeline [${order.orderId}]: ${order.status}")
                    val timeline = OrderTimeline(
                        orderId = order.orderId,
                        history = order.statusHistory,
                        currentStatus = order.status
                    )
                    trySend(Resource.Success(timeline))
                } else {
                    trySend(Resource.Error("Order not found"))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersForUser(userId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ordersCollection
            .whereEqualTo("customerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java).also { order ->
                            android.util.Log.d("ORDER_SYNC", "Realtime update received for customer: ${order?.status}")
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                // Sort in memory to avoid index requirements
                val sortedOrders = orders.sortedByDescending { it.createdAt }
                trySend(Resource.Success(sortedOrders))
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersForVendor(vendorId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ordersCollection
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java).also { order ->
                            android.util.Log.d("ORDER_SYNC", "Realtime update received for vendor: ${order?.status}")
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                // Sort in memory to avoid index requirements
                val sortedOrders = orders.sortedByDescending { it.createdAt }
                trySend(Resource.Success(sortedOrders))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(
        orderId: String, 
        status: OrderStatus, 
        actor: String = "SYSTEM",
        actorName: String = "",
        notes: String = ""
    ): Resource<Unit> {
        return try {
            val docRef = ordersCollection.document(orderId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentStatus = snapshot.getString("status")
                
                // Optional: Business logic check within transaction
                if (currentStatus == OrderStatus.CANCELLED.name || currentStatus == OrderStatus.DELIVERED.name) {
                    throw Exception("Cannot update status of a terminal order")
                }

                val historyEntry = OrderStatusHistory(
                    status = status,
                    actor = actor,
                    actorName = actorName,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                
                transaction.update(docRef, "status", status)
                transaction.update(docRef, "updatedAt", System.currentTimeMillis())
                transaction.update(docRef, "statusHistory", FieldValue.arrayUnion(historyEntry))
                
                null
            }.await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update order status")
        }
    }

    suspend fun updateDeliveryStatus(orderId: String, status: DeliveryStatus): Resource<Unit> {
        return try {
            ordersCollection.document(orderId).update(
                "deliveryStatus", status,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update delivery status")
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
