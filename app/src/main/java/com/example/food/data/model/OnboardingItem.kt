package com.example.food.data.model

data class OnboardingItem(
    val title: String,
    val description: String,
    val type: String, // "discover", "order", "personalized"
    val tags: List<String>
)
