package com.example.food.data.repository

import com.example.food.data.model.User
import com.example.food.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

open class UserRepository {
    protected open val firestore by lazy { FirebaseFirestore.getInstance() }
    protected open val auth by lazy { FirebaseAuth.getInstance() }

    open fun getUserProfile(): Flow<User?> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null

        // Listen to Auth State changes (Login/Logout)
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val userId = firebaseUser?.uid
            
            // If user changes, remove old listener
            snapshotListener?.remove()

            if (userId != null) {
                // If logged in, listen to Firestore document changes
                val userDocRef = firestore.collection("users").document(userId)
                
                snapshotListener = userDocRef.addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener

                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            trySend(user)
                        } else {
                            // Document doesn't exist — Create it from Firebase Auth info
                            val newUser = User(
                                userId = userId,
                                displayName = firebaseUser.displayName,
                                email = firebaseUser.email ?: "",
                                photoUrl = firebaseUser.photoUrl?.toString(),
                                role = UserRole.CUSTOMER // Default role
                            )
                            userDocRef.set(newUser)
                            trySend(newUser)
                        }
                    }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            snapshotListener?.remove()
        }
    }
}
