package com.example.food.core.util

import android.util.Patterns

object Validator {
    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Password rules:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one number
     * - At least one special character
     */
    fun validatePassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[<>\"']"), "")
    }

    fun validatePhone(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }
}
