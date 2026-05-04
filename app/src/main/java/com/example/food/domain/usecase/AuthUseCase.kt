package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.core.util.Validator
import com.example.food.data.model.User
import com.example.food.data.model.UserRole
import com.example.food.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCase(
    private val userRepository: UserRepository = UserRepository()
) {
    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        // Validation Layer
        if (!Validator.validateEmail(email)) {
            emit(Resource.Error("Invalid email address"))
            return@flow
        }
        
        try {
            // Service Logic
            userRepository.getUserProfile().collect { user ->
                if (user != null) {
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("User not found"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Login failed"))
        }
    }

    fun isAuthorized(user: User?, requiredRole: UserRole): Boolean {
        if (user == null) return false
        return when (requiredRole) {
            UserRole.ADMIN -> user.role == UserRole.ADMIN
            UserRole.VENDOR -> user.role == UserRole.VENDOR || user.role == UserRole.ADMIN
            UserRole.CUSTOMER -> true
        }
    }
}
