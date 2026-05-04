package com.example.food.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.domain.usecase.AuthUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AuthViewModel(
    private val authUseCase: AuthUseCase = AuthUseCase()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val WEB_CLIENT_ID = "11912048812-qstohuc1b0mmq3l5sb3bnd7s23r0eurf.apps.googleusercontent.com"

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)

                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authenticateWithFirebase(googleIdTokenCredential.idToken)
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }

            } catch (e: GetCredentialCancellationException) {
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
            }
        }
    }

    private suspend fun authenticateWithFirebase(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                syncUserToFirestore(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl?.toString()
                )
                
                _authState.value = AuthState.Success(user.uid, user.displayName)
            } else {
                _authState.value = AuthState.Error("Firebase user is null")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Firebase authentication failed")
        }
    }

    private suspend fun syncUserToFirestore(
        userId: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        val userMap = hashMapOf(
            "userId" to userId,
            "displayName" to displayName,
            "email" to email,
            "photoUrl" to photoUrl,
            "lastLogin" to System.currentTimeMillis(),
            "role" to "CUSTOMER" // Default role
        )

        try {
            firestore.collection("users").document(userId).set(userMap).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
