package com.example.food.data.repository

import com.example.food.data.model.Payment
import com.example.food.data.model.PaymentStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PaymentRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val paymentsCollection = firestore.collection("payments")

    suspend fun createPayment(payment: Payment): Result<Unit> {
        return try {
            paymentsCollection.document(payment.paymentId).set(payment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus, transactionRef: String? = null): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            transactionRef?.let { updates["transactionRef"] = it }
            
            paymentsCollection.document(paymentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentByOrderId(orderId: String): Payment? {
        return try {
            val snapshot = paymentsCollection.whereEqualTo("orderId", orderId)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Payment::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
