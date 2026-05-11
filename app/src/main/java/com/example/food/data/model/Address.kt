package com.example.food.data.model

import com.google.firebase.firestore.Exclude

data class Address(
    @get:Exclude val id: String = java.util.UUID.randomUUID().toString(),
    val addressId: String = "", // Firestore document ID
    val label: String = "Home", // Home, Work, Custom
    val city: String = "Addis Ababa",
    val subcity: String = "",
    val woreda: String = "",
    val kebele: String = "",
    val street: String = "",
    val landmark: String = "",
    val phoneNumber: String = "",
    val isDefault: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)
