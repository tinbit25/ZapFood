package com.example.food.data.repository

import android.util.Log
import com.example.food.data.model.Vendor
import com.google.firebase.firestore.FirebaseFirestore
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
