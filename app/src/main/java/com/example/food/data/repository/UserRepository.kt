package com.example.food.data.repository

import com.example.food.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserProfile(): Flow<User?> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null

        // Listen to Auth State changes (Login/Logout)
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            
            // If user changes, remove old listener
            snapshotListener?.remove()

            if (userId != null) {
                // If logged in, listen to Firestore document changes
                snapshotListener = firestore.collection("users")
                    .document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            trySend(user)
                        } else {
                            trySend(null)
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
