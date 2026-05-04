package com.example.food.ui.screens.auth

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val userId: String, val displayName: String?) : AuthState
    data class Error(val message: String) : AuthState
}
