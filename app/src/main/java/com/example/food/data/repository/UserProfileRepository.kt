package com.example.food.data.repository

import android.net.Uri
import com.example.food.data.model.User
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserProfileRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun updateProfile(user: User) {
        usersCollection.document(user.userId).set(user).await()
    }

    /**
     * Option 1: Base64 Workaround
     * Encodes image to Base64 string to store in Firestore, avoiding Firebase Storage billing.
     */
    fun uploadProfilePicture(userId: String, imageUri: Uri, contentResolver: android.content.ContentResolver): Flow<UploadStatus> = callbackFlow {
        try {
            trySend(UploadStatus.Progress(20))
            
            val inputStream = contentResolver.openInputStream(imageUri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                trySend(UploadStatus.Error("Could not decode image"))
                close()
                return@callbackFlow
            }

            trySend(UploadStatus.Progress(50))

            // Step 1: Resize for efficiency (Profile pics don't need to be 4K)
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                originalBitmap, 300, 300, true
            )

            // Step 2: Compress to JPEG
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            trySend(UploadStatus.Progress(80))

            // Step 3: Convert to Base64 String
            val base64String = "data:image/jpeg;base64," + android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            // Step 4: Update Firestore directly
            updatePhotoUrl(userId, base64String)

            trySend(UploadStatus.Success(base64String))
            close()
        } catch (e: Exception) {
            trySend(UploadStatus.Error("Encoding failed: ${e.localizedMessage}"))
            close(e)
        }

        awaitClose { /* No task to cancel */ }
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
