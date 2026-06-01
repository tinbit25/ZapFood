package com.example.food.core.util

object Validator {
    fun validateEmail(email: String): Boolean {
        return ValidationUtils.isValidEmail(email)
    }

    fun validatePassword(password: String): Boolean {
        return ValidationUtils.isValidPassword(password)
    }

    fun sanitizeInput(input: String): String {
        return ValidationUtils.sanitizeInput(input)
    }

    fun validatePhone(phone: String): Boolean {
        return ValidationUtils.isValidPhone(phone)
    }
}
