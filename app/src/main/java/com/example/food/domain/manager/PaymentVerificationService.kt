package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentVerificationService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun verifyAndTransitionOrder(orderId: String, checkPaymentSuccess: Boolean): Resource<Unit> {
        return try {
            val orderRef = firestore.collection("orders").document(orderId)
            val orderSnapshot = orderRef.get().await()
            if (!orderSnapshot.exists()) {
                return Resource.Error("Order not found")
            }

            if (checkPaymentSuccess) {
                firestore.runTransaction { transaction ->
                    transaction.update(orderRef, "orderStatus", OrderStatus.PAID.name)
                }.await()

                OrderReleaseManager().releaseOrderToVendor(orderId)
            } else {
                firestore.runTransaction { transaction ->
                    transaction.update(orderRef, "orderStatus", OrderStatus.CANCELLED.name)
                }.await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to verify and transition order")
        }
    }
}
