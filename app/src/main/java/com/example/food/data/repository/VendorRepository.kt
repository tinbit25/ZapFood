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

    /**
     * Registers a new vendor or updates an existing one.
     */
    suspend fun registerVendor(vendor: Vendor): Boolean {
        return try {
            vendorsCollection.document(vendor.id).set(vendor).await()
            true
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error registering vendor: ${e.message}")
            false
        }
    }

    /**
     * Retrieves a vendor by their User ID.
     */
    suspend fun getVendorByUserId(userId: String): Vendor? {
        return try {
            val query = vendorsCollection.whereEqualTo("userId", userId).get().await()
            query.documents.firstOrNull()?.toObject(Vendor::class.java)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error fetching vendor by userId: ${e.message}")
            null
        }
    }

    /**
     * Retrieves a vendor by their ID.
     */
    suspend fun getVendorById(vendorId: String): Vendor? {
        return try {
            vendorsCollection.document(vendorId).get().await().toObject(Vendor::class.java)
        } catch (e: Exception) {
            Log.e("VendorRepository", "Error fetching vendor by id: ${e.message}")
            null
        }
    }

    /**
     * Hybrid Search: Filter vendors by type or service tag.
     */
    fun getFilteredVendors(
        type: VendorType? = null,
        tag: ServiceTag? = null,
        queryText: String? = null
    ): Flow<Resource<List<Vendor>>> = callbackFlow {
        trySend(Resource.Loading())

        var query: Query = vendorsCollection

        // Multi-type support: businessTypes is a Set, so we use array-contains
        type?.let { query = query.whereArrayContains("businessTypes", it.name) }
        
        // Multi-service support: serviceTags is a Set
        tag?.let { query = query.whereArrayContains("serviceTags", it.name) }

        if (!queryText.isNullOrBlank()) {
            query = query.whereGreaterThanOrEqualTo("businessName", queryText)
                .whereLessThanOrEqualTo("businessName", queryText + "\uf8ff")
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                return@addSnapshotListener
            }

            val vendors = snapshot?.documents?.mapNotNull { it.toObject(Vendor::class.java) } ?: emptyList()
            trySend(Resource.Success(vendors))
        }

        awaitClose { listener.remove() }
    }

    /**
     * Updates specific verification info (Admin-only capability).
     */
    suspend fun updateVerificationStatus(vendorId: String, status: com.example.food.data.model.VerificationStatus): Boolean {
        return try {
            vendorsCollection.document(vendorId).update("verificationStatus", status).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
