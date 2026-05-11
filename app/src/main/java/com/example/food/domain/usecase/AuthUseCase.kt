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
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthUseCase(
    context: Context,
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) {
    private val securityManager = SecurityManager(context)

    suspend fun register(fullName: String, email: String, password: String, role: UserRole): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        val sanitizedName = Validator.sanitizeInput(fullName)
        val sanitizedEmail = Validator.sanitizeInput(email)
        
        if (sanitizedName.isEmpty()) {
            emit(Resource.Error("Full name is required"))
            return@flow
        }
        if (!Validator.validateEmail(sanitizedEmail)) {
            emit(Resource.Error("Invalid email address"))
            return@flow
        }
        if (password.isEmpty()) {
            emit(Resource.Error("Password is required"))
            return@flow
        }
        if (!Validator.validatePassword(password)) {
            emit(Resource.Error("Password too weak (8+ chars, upper, lower, number, special)"))
            return@flow
        }

        val passwordHash = securityManager.hashPassword(password)
        val id = UUID.randomUUID().toString()
        val newUser = User(
            id = id,
            userId = id,
            displayName = sanitizedName,
            email = sanitizedEmail,
            role = role,
            passwordHash = passwordHash
        )
        
        emit(authRepository.registerUser(newUser, password))
    }

    suspend fun login(email: String, password: String): Flow<Resource<Pair<User, AuthToken>>> = flow {
        emit(Resource.Loading())
        
        val sanitizedEmail = Validator.sanitizeInput(email)
        if (sanitizedEmail.isEmpty() || password.isEmpty()) {
            emit(Resource.Error("Email and password are required"))
            return@flow
        }

        try {
            val authResult = com.google.firebase.auth.FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(sanitizedEmail, password)
                .await()
            val firebaseUid = authResult.user?.uid
                ?: run { emit(Resource.Error("Login failed: no user returned")); return@flow }

            // Fetch the Firestore profile for this user
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(firebaseUid).get().await()
            val user = doc.toObject(User::class.java)
                ?: run { emit(Resource.Error("User profile not found")); return@flow }

            if (!user.isActive) {
                emit(Resource.Error("Your account has been deactivated. Please contact support."))
                return@flow
            }

            // Access token (15 mins), Refresh token (7 days)
            val accessToken = securityManager.generateSimulatedToken(firebaseUid, user.role.name, 15)
            val refreshToken = securityManager.generateSimulatedToken(firebaseUid, user.role.name, 10080)
            val tokens = AuthToken(accessToken, refreshToken, System.currentTimeMillis() + (15 * 60 * 1000))
            
            val session = AuthSession(userId = firebaseUid, expiryTimestamp = tokens.expiryTimestamp)
            securityManager.saveTokens(tokens, session.sessionId)
            authRepository.saveSession(session)

            emit(Resource.Success(Pair(user, tokens)))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Login failed"))
        }
    }

    fun isAuthorized(user: User?, requiredRole: UserRole): Boolean {
        if (user == null) return false
        return when (requiredRole) {
            UserRole.ADMIN -> user.role == UserRole.ADMIN
            UserRole.VENDOR -> user.role == UserRole.VENDOR || user.role == UserRole.ADMIN
            UserRole.CUSTOMER -> true // Everyone can access customer features
        }
    }

    /**
     * Refresh the access token using a valid refresh token.
     */
    suspend fun refreshAccessToken(): Flow<Resource<AuthToken>> = flow {
        emit(Resource.Loading())
        val tokens = securityManager.getTokens()
        
        if (tokens == null) {
            emit(Resource.Error("No tokens found. Please login again."))
            return@flow
        }

        if (securityManager.isTokenExpired(tokens.refreshToken)) {
            emit(Resource.Error("Refresh token expired. Please login again."))
            securityManager.clearTokens()
            return@flow
        }

        val payload = securityManager.decodeSimulatedToken(tokens.refreshToken)
        if (payload == null) {
            emit(Resource.Error("Invalid refresh token."))
            return@flow
        }

        val userId = payload["userId"]!!
        val role = payload["role"]!!

        // Generate new access token (15 mins) and keep same refresh token for now
        val newAccessToken = securityManager.generateSimulatedToken(userId, role, 15)
        val newTokens = tokens.copy(
            accessToken = newAccessToken,
            expiryTimestamp = System.currentTimeMillis() + (15 * 60 * 1000)
        )
        
        securityManager.saveTokens(newTokens, securityManager.getSessionId())
        emit(Resource.Success(newTokens))
    }

    /**
     * Permission Checks
     */
    fun canCreateMeal(user: User?): Boolean = isAuthorized(user, UserRole.VENDOR)
    fun canPlaceOrder(user: User?): Boolean = isAuthorized(user, UserRole.CUSTOMER)
    fun canApproveVendor(user: User?): Boolean = isAuthorized(user, UserRole.ADMIN)
    fun canManageSystem(user: User?): Boolean = isAuthorized(user, UserRole.ADMIN)

    suspend fun requestPasswordReset(email: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        
        val user = authRepository.findUserByEmail(email)
        if (user == null) {
            emit(Resource.Error("No account found with this email"))
            return@flow
        }

        val token = UUID.randomUUID().toString()
        val resetToken = ResetToken(
            token = token, 
            userId = user.userId, 
            expiryTimestamp = System.currentTimeMillis() + (15 * 60 * 1000) // 15 mins
        )
        authRepository.saveResetToken(resetToken)
        
        // In a production app, we would use Firebase Auth's sendPasswordResetEmail
        // but since we are mirroring logic, we provide a custom token flow.
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

    suspend fun changePassword(userId: String, oldPassword: String, newPassword: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            // 1. Fetch current profile to verify old password hash
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
                ?: run { emit(Resource.Error("User profile not found")); return@flow }

            // 2. Verify old password
            if (!securityManager.verifyPassword(oldPassword, user.passwordHash)) {
                emit(Resource.Error("Incorrect old password"))
                return@flow
            }

            // 3. Validate new password strength
            if (!Validator.validatePassword(newPassword)) {
                emit(Resource.Error("New password too weak"))
                return@flow
            }

            // 4. Update Firebase Auth password
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null && firebaseUser.uid == userId) {
                firebaseUser.updatePassword(newPassword).await()
            } else {
                emit(Resource.Error("Session error: Please re-login"))
                return@flow
            }

            // 5. Update Firestore hash
            val newHash = securityManager.hashPassword(newPassword)
            authRepository.updateUserPassword(userId, newHash)

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Password change failed"))
        }
    }

    suspend fun loginWithPhone(credential: com.google.firebase.auth.PhoneAuthCredential): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val result = authRepository.loginWithPhone(credential)
            if (result is Resource.Error) {
                emit(Resource.Error(result.message ?: "Phone login failed"))
                return@flow
            }

            val firebaseUser = result.data!!
            val phone = firebaseUser.phoneNumber ?: ""
            
            // Check if user profile exists
            var user = authRepository.findUserByPhone(phone)
            
            if (user == null) {
                // Create new user profile
                user = User(
                    userId = firebaseUser.uid,
                    phoneNumber = phone,
                    displayName = "User ${phone.takeLast(4)}",
                    role = UserRole.CUSTOMER,
                    isActive = true
                )
                authRepository.createUserProfile(user)
            }

            if (!user.isActive) {
                emit(Resource.Error("Your account has been deactivated"))
                return@flow
            }

            // Create Session & Tokens
            val accessToken = securityManager.generateSimulatedToken(user.userId, user.role.name, 15)
            val refreshToken = securityManager.generateSimulatedToken(user.userId, user.role.name, 10080)
            val tokens = AuthToken(accessToken, refreshToken, System.currentTimeMillis() + (15 * 60 * 1000))
            
            val session = AuthSession(userId = user.userId, expiryTimestamp = tokens.expiryTimestamp)
            securityManager.saveTokens(tokens, session.sessionId)
            authRepository.saveSession(session)

            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Phone login failed"))
        }
    }

    fun validateEthiopianPhone(phone: String): Boolean {
        val sanitized = phone.trim().replace(" ", "").replace("-", "")
        // Matches +2519xxxxxxxx, +2517xxxxxxxx, 09xxxxxxxx, 07xxxxxxxx, 9xxxxxxxx, 7xxxxxxxx
        val regex = "^(\\+251|0|)(9|7)\\d{8}$".toRegex()
        return sanitized.matches(regex)
    }

    suspend fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val sessionId = securityManager.getSessionId()
        if (sessionId != null) {
            authRepository.removeSession(sessionId)
        }
        securityManager.clearTokens()
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        emit(Resource.Success(Unit))
    }

    suspend fun logoutAllDevices(userId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val result = authRepository.removeAllUserSessions(userId)
        if (result is Resource.Success) {
            securityManager.clearTokens()
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        }
        emit(result)
    }
}
