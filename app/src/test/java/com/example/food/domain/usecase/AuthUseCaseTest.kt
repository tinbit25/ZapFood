package com.example.food.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.food.core.util.Resource
import com.example.food.data.model.UserRole
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthUseCaseTest {

    private lateinit var authUseCase: AuthUseCase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        authUseCase = AuthUseCase(context)
    }

    @Test
    fun `register with empty fields should return error`() = runBlocking {
        val flow = authUseCase.register("", "", "", UserRole.CUSTOMER)
        val results = flow.toList()
        
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Error)
        assertEquals("Full name is required", (results[1] as Resource.Error).message)
    }

    @Test
    fun `register with weak password should return error`() = runBlocking {
        val flow = authUseCase.register("John Doe", "john@example.com", "123", UserRole.CUSTOMER)
        val results = flow.toList()
        
        assertTrue(results[1] is Resource.Error)
        assertTrue((results[1] as Resource.Error).message!!.contains("Password too weak"))
    }

    @Test
    fun `login with empty fields should return error`() = runBlocking {
        val flow = authUseCase.login("", "")
        val results = flow.toList()
        
        assertTrue(results[1] is Resource.Error)
        assertEquals("Email and password are required", (results[1] as Resource.Error).message)
    }

    @Test
    fun `isAuthorized should correctly identify roles`() {
        val customer = com.example.food.data.model.User(role = UserRole.CUSTOMER)
        val vendor = com.example.food.data.model.User(role = UserRole.VENDOR)
        val admin = com.example.food.data.model.User(role = UserRole.ADMIN)

        // Customer permissions
        assertTrue(authUseCase.isAuthorized(customer, UserRole.CUSTOMER))
        assertFalse(authUseCase.isAuthorized(customer, UserRole.VENDOR))
        assertFalse(authUseCase.isAuthorized(customer, UserRole.ADMIN))

        // Vendor permissions
        assertTrue(authUseCase.isAuthorized(vendor, UserRole.CUSTOMER))
        assertTrue(authUseCase.isAuthorized(vendor, UserRole.VENDOR))
        assertFalse(authUseCase.isAuthorized(vendor, UserRole.ADMIN))

        // Admin permissions
        assertTrue(authUseCase.isAuthorized(admin, UserRole.CUSTOMER))
        assertTrue(authUseCase.isAuthorized(admin, UserRole.VENDOR))
        assertTrue(authUseCase.isAuthorized(admin, UserRole.ADMIN))
    }
}
