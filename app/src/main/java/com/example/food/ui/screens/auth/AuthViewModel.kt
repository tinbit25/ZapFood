package com.example.food.ui.screens.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.AuthUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AdvancedAuthState {
    object Idle : AdvancedAuthState()
    object Loading : AdvancedAuthState()
    data class Success(val userId: String, val role: UserRole) : AdvancedAuthState()
    data class Error(val message: String) : AdvancedAuthState()
    data class RecoverySent(val message: String) : AdvancedAuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authUseCase = AuthUseCase(application)

    private val _authState = MutableStateFlow<AdvancedAuthState>(AdvancedAuthState.Idle)
    val authState: StateFlow<AdvancedAuthState> = _authState.asStateFlow()

    private var loginAttempts = 0
    private var lastAttemptTime = 0L

    fun login(email: String, password: String) {
        if (isRateLimited()) return

        viewModelScope.launch {
            authUseCase.login(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _authState.value = AdvancedAuthState.Loading
                    is Resource.Success -> {
                        loginAttempts = 0
                        val (user, tokens) = resource.data!!
                        _authState.value = AdvancedAuthState.Success(user.userId, user.role)
                    }
                    is Resource.Error -> {
                        loginAttempts++
                        lastAttemptTime = System.currentTimeMillis()
                        _authState.value = AdvancedAuthState.Error(resource.message ?: "Login failed")
                    }
                }
            }
        }
    }

    fun register(fullName: String, email: String, password: String, role: UserRole) {
        viewModelScope.launch {
            authUseCase.register(fullName, email, password, role).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _authState.value = AdvancedAuthState.Loading
                    is Resource.Success -> {
                        _authState.value = AdvancedAuthState.Success(resource.data!!.userId, resource.data.role)
                    }
                    is Resource.Error -> {
                        _authState.value = AdvancedAuthState.Error(resource.message ?: "Registration failed")
                    }
                }
            }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            authUseCase.requestPasswordReset(email).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _authState.value = AdvancedAuthState.Loading
                    is Resource.Success -> _authState.value = AdvancedAuthState.RecoverySent(resource.data!!)
                    is Resource.Error -> _authState.value = AdvancedAuthState.Error(resource.message ?: "Failed to send reset link")
                }
            }
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            authUseCase.resetPassword(token, newPassword).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _authState.value = AdvancedAuthState.Loading
                    is Resource.Success -> _authState.value = AdvancedAuthState.Idle
                    is Resource.Error -> _authState.value = AdvancedAuthState.Error(resource.message ?: "Failed to reset password")
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AdvancedAuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("11912048812-qstohuc1b0mmq3l5sb3bnd7s23r0eurf.apps.googleusercontent.com")
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                when (credential) {
                    is GoogleIdTokenCredential -> {
                        val googleIdToken = credential.idToken
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                        val user = authResult.user
                        if (user != null) {
                            _authState.value = AdvancedAuthState.Success(user.uid, UserRole.CUSTOMER)
                        } else {
                            _authState.value = AdvancedAuthState.Error("Google Sign-In failed: No user returned")
                        }
                    }
                    else -> {
                        // Sometimes it comes back as a CustomCredential with the Google ID Token type
                        try {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val googleIdToken = googleIdTokenCredential.idToken
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                            val authResult = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
                            val user = authResult.user
                            if (user != null) {
                                _authState.value = AdvancedAuthState.Success(user.uid, UserRole.CUSTOMER)
                            } else {
                                _authState.value = AdvancedAuthState.Error("Google Sign-In failed: No user returned")
                            }
                        } catch (e: Exception) {
                            _authState.value = AdvancedAuthState.Error("Unexpected credential type: ${credential.type}")
                        }
                    }
                }
            } catch (e: Exception) {
                _authState.value = AdvancedAuthState.Error(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCase.logout().collect { resource ->
                if (resource is Resource.Success) {
                    _authState.value = AdvancedAuthState.Idle
                }
            }
        }
    }

    fun logoutAllDevices(userId: String) {
        viewModelScope.launch {
            authUseCase.logoutAllDevices(userId).collect { resource ->
                if (resource is Resource.Success) {
                    _authState.value = AdvancedAuthState.Idle
                }
            }
        }
    }

    private fun isRateLimited(): Boolean {
        if (loginAttempts >= 5) {
            val waitTime = (System.currentTimeMillis() - lastAttemptTime) / 1000
            if (waitTime < 60) {
                _authState.value = AdvancedAuthState.Error("Too many failed attempts. Try again in ${60 - waitTime} seconds.")
                return true
            } else {
                loginAttempts = 0 // Reset after wait
            }
        }
        return false
    }

    fun resetState() {
        _authState.value = AdvancedAuthState.Idle
    }
}
