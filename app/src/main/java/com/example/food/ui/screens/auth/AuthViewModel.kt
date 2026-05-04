package com.example.food.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class AuthViewModel : ViewModel() {

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

                // 1. Setup Google ID Option
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                // 2. Create the Request
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 3. Launch the UI to get the credential
                val result = credentialManager.getCredential(context, request)

                // 4. Handle the Credential Result
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        authenticateWithFirebase(idToken)
                    } catch (e: Exception) {
                        _authState.value = AuthState.Error("Failed parsing credential")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }

            } catch (e: GetCredentialCancellationException) {
                _authState.value = AuthState.Idle // User cancelled
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
                
                _authState.value = AuthState.Success(
                    userId = user.uid,
                    displayName = user.displayName
                )
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
            "favoriteCategories" to emptyList<String>(),
            "orderHistory" to emptyList<String>()
        )

        try {
            firestore.collection("users").document(userId)
                .set(userMap)
                .await()
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
