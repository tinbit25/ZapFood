package com.example.food.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar

@Composable
fun ResetPasswordScreen(
    token: String,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Reset Password")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set new password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enter a strong password to secure your account.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "New Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Update Password",
                onClick = { authViewModel.resetPassword(token, newPassword) },
                enabled = newPassword.isNotEmpty() && newPassword == confirmPassword && authState !is AdvancedAuthState.Loading,
                backgroundColor = Color(0xFFF16B24)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (authState) {
                is AdvancedAuthState.Loading -> CircularProgressIndicator(color = Color(0xFFF16B24))
                is AdvancedAuthState.Idle -> {
                    // Success state if we navigated from ResetPassword back to Idle
                    Text(
                        text = "Password updated successfully!",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Back to Login", color = Color(0xFFF16B24))
                    }
                }
                is AdvancedAuthState.Error -> {
                    Text(
                        text = (authState as AdvancedAuthState.Error).message,
                        color = Color(0xFFE57373),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                else -> {}
            }
        }
    }
}
