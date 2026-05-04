package com.example.food.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Forgot Password", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recover your password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your email address to receive a recovery link.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Send Reset Link",
                onClick = { authViewModel.requestPasswordReset(email) },
                enabled = email.isNotEmpty() && authState !is AdvancedAuthState.Loading,
                backgroundColor = Color(0xFFF16B24)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (authState) {
                is AdvancedAuthState.Loading -> CircularProgressIndicator(color = Color(0xFFF16B24))
                is AdvancedAuthState.RecoverySent -> {
                    Text(
                        text = (authState as AdvancedAuthState.RecoverySent).message,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
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
