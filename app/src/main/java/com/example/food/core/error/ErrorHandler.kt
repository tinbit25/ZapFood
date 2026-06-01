package com.example.food.core.error

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorHandler {

    fun getReadableMessage(e: Exception): String {
        return when (e) {
            // Authentication Errors
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password. Please try again."
            is FirebaseAuthInvalidUserException -> "No account found with this email. Please sign up."
            is FirebaseAuthUserCollisionException -> "An account already exists with this email."
            is FirebaseAuthRecentLoginRequiredException -> "For your security, please log in again to perform this action."
            is FirebaseAuthException -> mapAuthErrorCode(e.errorCode)

            // Firestore Errors
            is FirebaseFirestoreException -> mapFirestoreErrorCode(e.code)

            // Network Errors
            is UnknownHostException, is ConnectException -> "Internet connection lost. Please check your network and try again."
            is SocketTimeoutException -> "The connection timed out. Please try again later."

            // General / Unknown Errors
            else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }

    private fun mapAuthErrorCode(errorCode: String): String {
        return when (errorCode) {
            "ERROR_WEAK_PASSWORD" -> "Your password is too weak. Please use a stronger password."
            "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
            "ERROR_USER_NOT_FOUND" -> "Account not found."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is currently disabled."
            else -> "Authentication failed. Please try again."
        }
    }

    private fun mapFirestoreErrorCode(code: FirebaseFirestoreException.Code): String {
        return when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "You do not have permission to perform this action."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "The service is currently unavailable. Please check your connection."
            FirebaseFirestoreException.Code.NOT_FOUND -> "The requested information could not be found."
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> "This item already exists."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "The request took too long. Please try again."
            else -> "A database error occurred. Please try again."
        }
    }
}
