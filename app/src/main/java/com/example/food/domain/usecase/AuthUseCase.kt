package com.example.food.domain.usecase

import android.content.Context
import com.example.food.core.security.SecurityManager
import com.example.food.core.util.Resource
import com.example.food.core.util.Validator
import com.example.food.data.model.*
import com.example.food.data.repository.AuthRepository
import com.example.food.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AuthUseCase(
    context: Context,
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) {
    private val securityManager = SecurityManager(context)

    suspend fun register(fullName: String, email: String, password: String, role: UserRole): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        if (!Validator.validateEmail(email)) {
            emit(Resource.Error("Invalid email"))
            return@flow
        }
        if (!Validator.validatePassword(password)) {
            emit(Resource.Error("Password too weak"))
            return@flow
        }

        val passwordHash = securityManager.hashPassword(password)
        val id = UUID.randomUUID().toString()
        val newUser = User(
            id = id,
            userId = id,
            displayName = fullName,
            email = email,
            role = role,
            passwordHash = passwordHash
        )
        
        emit(authRepository.registerUser(newUser))
    }

    suspend fun login(email: String, password: String): Flow<Resource<Pair<User, AuthToken>>> = flow {
        emit(Resource.Loading())
        
        userRepository.getUserProfile().collect { user ->
            if (user == null || user.email != email) {
                emit(Resource.Error("User not found"))
                return@collect
            }

            if (!securityManager.verifyPassword(password, user.passwordHash)) {
                emit(Resource.Error("Invalid credentials"))
                return@collect
            }

            // Generate Tokens
            val accessToken = securityManager.generateSimulatedToken(user.userId, user.role.name, 15)
            val refreshToken = securityManager.generateSimulatedToken(user.userId, user.role.name, 10080) // 7 days
            val tokens = AuthToken(accessToken, refreshToken, System.currentTimeMillis() + (15 * 60 * 1000))
            
            securityManager.saveTokens(tokens)

            // Track Session
            val session = AuthSession(userId = user.userId, expiryTimestamp = tokens.expiryTimestamp)
            authRepository.saveSession(session)

            emit(Resource.Success(Pair(user, tokens)))
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

    /**
     * Permission Checks
     */
    fun canCreateMeal(user: User?): Boolean = isAuthorized(user, UserRole.VENDOR)
    fun canPlaceOrder(user: User?): Boolean = isAuthorized(user, UserRole.CUSTOMER)
    fun canApproveVendor(user: User?): Boolean = isAuthorized(user, UserRole.ADMIN)

    suspend fun requestPasswordReset(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        // In real app, search for user by email first
        val token = UUID.randomUUID().toString()
        val resetToken = ResetToken(token = token, userId = "unknown_yet", expiryTimestamp = System.currentTimeMillis() + 900000)
        authRepository.saveResetToken(resetToken)
        
        // Mock email sending
        emit(Resource.Success("Reset link sent to $email (Token: $token)"))
    }

    suspend fun resetPassword(token: String, newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val resetToken = authRepository.getResetToken(token)
        
        if (resetToken == null || resetToken.isUsed || System.currentTimeMillis() > resetToken.expiryTimestamp) {
            emit(Resource.Error("Invalid or expired token"))
            return@flow
        }

        if (!Validator.validatePassword(newPassword)) {
            emit(Resource.Error("Password too weak"))
            return@flow
        }

        val newHash = securityManager.hashPassword(newPassword)
        val result = authRepository.updateUserPassword(resetToken.userId, newHash)
        
        if (result is Resource.Success) {
            // Mark token as used
            authRepository.saveResetToken(resetToken.copy(isUsed = true))
        }
        emit(result)
    }

    fun logout() {
        securityManager.clearTokens()
    }
}
