package com.example.food.core.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecurityManagerTest {

    private lateinit var securityManager: SecurityManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        securityManager = SecurityManager(context)
    }

    @Test
    fun testPasswordHashing() {
        val password = "StrongPassword123!"
        val hash = securityManager.hashPassword(password)
        
        assertNotNull(hash)
        assertTrue(securityManager.verifyPassword(password, hash))
        assertFalse(securityManager.verifyPassword("wrong", hash))
    }

    @Test
    fun testTokenSimulation() {
        val userId = "user123"
        val role = "VENDOR"
        val token = securityManager.generateSimulatedToken(userId, role, 15)
        
        assertNotNull(token)
        val decoded = securityManager.decodeSimulatedToken(token)
        
        assertEquals(userId, decoded?.get("userId"))
        assertEquals(role, decoded?.get("role"))
        assertFalse(securityManager.isTokenExpired(token))
    }
}
