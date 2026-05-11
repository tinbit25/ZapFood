package com.example.food.data.repository

import android.net.Uri
import com.example.food.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun updateProfile(user: User) {
        usersCollection.document(user.userId).set(user).await()
    }

    fun uploadProfilePicture(userId: String, imageUri: Uri): Flow<UploadStatus> = callbackFlow {
        val storageRef = storage.reference.child("profile_pictures/$userId.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                trySend(UploadStatus.Success(uri.toString()))
                close()
            }
        }.addOnFailureListener { e: Exception ->
            trySend(UploadStatus.Error(e.toString()))
            close(e)
        }

        awaitClose { uploadTask.cancel() }
    }

    suspend fun updatePhotoUrl(userId: String, photoUrl: String) {
        usersCollection.document(userId).update("photoUrl", photoUrl).await()
    }
}

sealed class UploadStatus {
    data class Progress(val percentage: Int) : UploadStatus()
    data class Success(val downloadUrl: String) : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}
