package com.example.food.core.error

object ValidationMapper {
    fun getPasswordErrorMessage(password: String): String? {
        if (password.length < 6) return "Password is too short. Minimum 6 characters required."
        return null
    }

    fun getEmailErrorMessage(email: String): String? {
        if (email.isBlank()) return "Email address cannot be empty."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address."
        }
        return null
    }

    fun getDisplayNameErrorMessage(name: String): String? {
        if (name.isBlank()) return "Please enter your full name."
        if (name.length < 2) return "Name must be at least 2 characters long."
        return null
    }

    fun getVendorLicenseErrorMessage(uriString: String?): String? {
        if (uriString.isNullOrBlank()) return "Please upload your vendor license."
        return null
    }

    fun getGenericRequiredFieldError(fieldName: String): String {
        return "Please provide your $fieldName."
    }
}
