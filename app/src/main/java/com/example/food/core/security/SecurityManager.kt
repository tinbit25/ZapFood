package com.example.food.core.security

import android.content.Context
import android.util.Base64
import com.example.food.data.model.AuthToken
import java.security.MessageDigest
import java.util.UUID

class SecurityManager(context: Context) {
    
    private val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    /**
     * Simulates JWT generation: base64(userId:role:expiry)
     */
    fun generateSimulatedToken(userId: String, role: String, expiryMinutes: Int): String {
        val expiry = System.currentTimeMillis() + (expiryMinutes * 60 * 1000)
        val payload = "$userId:$role:$expiry"
        return Base64.encodeToString(payload.toByteArray(), Base64.DEFAULT)
    }

    fun decodeSimulatedToken(token: String): Map<String, String>? {
        return try {
            val decoded = String(Base64.decode(token, Base64.DEFAULT))
            val parts = decoded.split(":")
            if (parts.size == 3) {
                mapOf(
                    "userId" to parts[0],
                    "role" to parts[1],
                    "expiry" to parts[2]
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenExpired(token: String): Boolean {
        val payload = decodeSimulatedToken(token) ?: return true
        val expiry = payload["expiry"]?.toLong() ?: 0L
        return System.currentTimeMillis() > expiry
    }

    fun saveTokens(tokens: AuthToken, sessionId: String? = null) {
        prefs.edit().apply {
            putString("access_token", tokens.accessToken)
            putString("refresh_token", tokens.refreshToken)
            putLong("token_expiry", tokens.expiryTimestamp)
            if (sessionId != null) putString("session_id", sessionId)
            apply()
        }
    }

    fun getSessionId(): String? = prefs.getString("session_id", null)

    fun getTokens(): AuthToken? {
        val access = prefs.getString("access_token", null) ?: return null
        val refresh = prefs.getString("refresh_token", null) ?: return null
        val expiry = prefs.getLong("token_expiry", 0L)
        return AuthToken(access, refresh, expiry)
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
