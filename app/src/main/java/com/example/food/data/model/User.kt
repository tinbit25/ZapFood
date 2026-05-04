package com.example.food.data.model

data class User(
    val userId: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val favoriteCategories: List<String> = emptyList(),
    val orderHistory: List<String> = emptyList()
)
