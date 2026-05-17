package com.example.food.domain.manager

import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class VendorContentSyncEngine {
    private val firestore = FirebaseFirestore.getInstance()

    fun observeVendorStatus(vendorId: String): Flow<Vendor?> = callbackFlow {
        val listener = firestore.collection("vendors")
            .document(vendorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val vendor = snapshot?.toObject(Vendor::class.java)
                trySend(vendor)
            }
        awaitClose { listener.remove() }
    }

    fun observeMealAvailability(mealId: String): Flow<Meal?> = callbackFlow {
        val listener = firestore.collection("meals")
            .document(mealId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val meal = snapshot?.toObject(Meal::class.java)
                trySend(meal)
            }
        awaitClose { listener.remove() }
    }
}
