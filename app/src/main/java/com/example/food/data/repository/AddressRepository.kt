package com.example.food.data.repository

import com.example.food.data.model.Address
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AddressRepository {
    private val firestore = FirebaseFirestore.getInstance()

    private fun getAddressesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("addresses")

    suspend fun getAddresses(userId: String): List<Address> {
        return getAddressesCollection(userId).get().await().toObjects(Address::class.java)
    }

    suspend fun addAddress(userId: String, address: Address) {
        val collection = getAddressesCollection(userId)
        val docRef = if (address.addressId.isEmpty()) collection.document() else collection.document(address.addressId)
        
        val finalAddress = address.copy(addressId = docRef.id)
        
        if (finalAddress.isDefault) {
            clearDefaultAddress(userId)
        }
        
        docRef.set(finalAddress).await()
    }

    suspend fun deleteAddress(userId: String, addressId: String) {
        getAddressesCollection(userId).document(addressId).delete().await()
    }

    suspend fun setDefaultAddress(userId: String, addressId: String) {
        clearDefaultAddress(userId)
        getAddressesCollection(userId).document(addressId).update("isDefault", true).await()
    }

    private suspend fun clearDefaultAddress(userId: String) {
        val collection = getAddressesCollection(userId)
        val defaultAddresses = collection.whereEqualTo("isDefault", true).get().await()
        for (doc in defaultAddresses.documents) {
            doc.reference.update("isDefault", false).await()
        }
    }
}
