package com.example.food.data.repository

import android.util.Log
import com.example.food.core.util.Resource
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.data.model.ServiceTag
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class VendorRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val vendorsCollection = firestore.collection("vendors")

    suspend fun registerVendor(vendor: Vendor): Boolean {
        return try {
            // Use userId as the document ID to prevent duplicates
            vendorsCollection.document(vendor.userId).set(vendor).await()
            true
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error registering vendor: ${e.message}")
            false
        }
    }

    suspend fun getVendorByUserId(userId: String): Vendor? {
        return try {
            val query = vendorsCollection.whereEqualTo("userId", userId).get().await()
            query.documents.firstOrNull()?.toObject(Vendor::class.java)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error fetching vendor by userId: ${e.message}")
            null
        }
    }

    /** Real-time listener — emits every time the vendor doc changes in Firestore */
    fun listenToVendorByUserId(userId: String): kotlinx.coroutines.flow.Flow<Vendor?> = callbackFlow {
        val listener = vendorsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("VendorRepository", "Vendor listener error: ${error.message}")
                    trySend(null)
                    return@addSnapshotListener
                }
                val vendor = snapshot?.documents?.firstOrNull()?.let { doc ->
                    try { doc.toObject(Vendor::class.java) } catch (e: Exception) { null }
                }
                trySend(vendor)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getVendorById(vendorId: String): Vendor? {
        return try {
            vendorsCollection.document(vendorId).get().await().toObject(Vendor::class.java)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error fetching vendor by id: ${e.message}")
            null
        }
    }

    /**
     * Comprehensive Search and Filter
     */
    fun getVendors(
        type: VendorType? = null,
        tag: ServiceTag? = null,
        queryText: String? = null,
        sortBy: String? = null, // "rating", "createdAt", "totalOrders"
        limit: Long = 20
    ): Flow<Resource<List<Vendor>>> = callbackFlow {
        trySend(Resource.Loading())

        var query: Query = vendorsCollection

        type?.let { query = query.whereArrayContains("businessTypes", it.name) }
        tag?.let { query = query.whereArrayContains("serviceTags", it.name) }

        if (!queryText.isNullOrBlank()) {
            query = query.whereGreaterThanOrEqualTo("businessName", queryText)
                .whereLessThanOrEqualTo("businessName", queryText + "\uf8ff")
        }

        sortBy?.let {
            query = query.orderBy(it, Query.Direction.DESCENDING)
        }

        query = query.limit(limit)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                return@addSnapshotListener
            }

            val vendors = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Vendor::class.java)
                } catch (e: Exception) {
                    Log.e("VendorRepository", "Error parsing vendor ${doc.id}: ${e.message}")
                    null
                }
            } ?: emptyList()
            trySend(Resource.Success(vendors))
        }

        awaitClose { listener.remove() }
    }

    /**
     * Specific Section Queries
     */
    fun getTopRatedVendors() = getVendors(sortBy = "rating", limit = 10)
    fun getPopularVendors() = getVendors(sortBy = "totalOrders", limit = 10)
    fun getNewVendors() = getVendors(sortBy = "createdAt", limit = 10)
    
    suspend fun updateVerificationStatus(vendorId: String, status: com.example.food.data.model.VerificationStatus): Boolean {
        return try {
            vendorsCollection.document(vendorId).update("verificationStatus", status).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
