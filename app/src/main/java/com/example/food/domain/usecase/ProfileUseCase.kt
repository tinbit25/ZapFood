package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.core.util.Validator
import com.example.food.data.model.User
import com.example.food.data.model.UserRole
import com.example.food.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProfileUseCase(
    private val userRepository: UserRepository = UserRepository()
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun updateProfile(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        // Sanitize and Validate
        val sanitizedDisplayName = Validator.sanitizeInput(user.displayName ?: "")
        
        try {
            firestore.collection("users").document(user.userId).set(user.copy(displayName = sanitizedDisplayName)).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Profile update failed"))
        }
    }

    suspend fun updateCustomerPreferences(userId: String, preferences: List<String>, dietaryNeeds: List<String>): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "preferences" to preferences,
                    "dietaryNeeds" to dietaryNeeds
                )
            ).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Update failed"))
        }
    }

    suspend fun updateVendorBusinessInfo(userId: String, cuisineType: String, address: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("users").document(userId).update(
                mapOf(
                    "cuisineType" to cuisineType,
                    "businessAddress" to address
                )
            ).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Update failed"))
        }
    }
}
