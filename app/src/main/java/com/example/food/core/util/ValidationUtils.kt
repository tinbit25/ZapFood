package com.example.food.core.util

import android.util.Patterns

object ValidationUtils {
    fun validateEmail(email: String?): String? {
        val trimmed = email?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return "Email is required"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return "Email is invalid"
        }
        return null
    }

    fun validatePassword(password: String?): String? {
        val pw = password.orEmpty()
        if (pw.isEmpty()) {
            return "Password is required"
        }
        if (pw.length < 6) {
            return "Password must be at least 6 characters"
        }
        if (!pw.any { it.isLetter() }) {
            return "Password must contain at least 1 letter"
        }
        if (!pw.any { it.isDigit() }) {
            return "Password must contain at least 1 number"
        }
        return null
    }

    fun validateFullName(fullName: String?): String? {
        val trimmed = fullName?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return "Full name is required"
        }
        return null
    }

    fun validatePhone(phone: String?): String? {
        val trimmed = phone?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return "Phone number is required"
        }
        // Matches +2519xxxxxxxx, +2517xxxxxxxx, 09xxxxxxxx, 07xxxxxxxx, 9xxxxxxxx, 7xxxxxxxx
        val regex = "^(\\+251|0|)(9|7)\\d{8}$".toRegex()
        val sanitized = trimmed.replace(" ", "").replace("-", "")
        if (!sanitized.matches(regex)) {
            return "Phone number is invalid"
        }
        return null
    }

    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[<>\"']"), "")
    }

    fun isValidEmail(email: String?): Boolean = validateEmail(email) == null
    fun isValidPassword(password: String?): Boolean = validatePassword(password) == null
    fun isValidPhone(phone: String?): Boolean = validatePhone(phone) == null
    fun isValidFullName(fullName: String?): Boolean = validateFullName(fullName) == null
}
