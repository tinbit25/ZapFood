package com.example.food.data.repository

import com.example.food.data.model.Vendor
import com.example.food.data.model.VerificationStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRealtimeRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val vendorsCollection = firestore.collection("vendors")

    fun observeVendorState(userId: String): Flow<Vendor?> = callbackFlow {
        val listener = vendorsCollection
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val vendor = try {
                    snapshot?.toObject(Vendor::class.java)
                } catch (e: Exception) {
                    null
                }
                trySend(vendor)
            }
        awaitClose { listener.remove() }
    }

    suspend fun completeOnboarding(vendorId: String): Boolean {
        return try {
            vendorsCollection.document(vendorId).update(
                mapOf(
                    "profileCompleted" to true,
                    "verificationStatus" to VerificationStatus.PENDING_REVIEW.name
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
