package com.example.food.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import android.app.Activity
import kotlinx.coroutines.delay

@Composable
fun OTPVerificationScreen(
    phoneNumber: String,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    isLinking: Boolean = false,
    viewModel: AuthViewModel = viewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    val phoneState by viewModel.phoneAuthState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    
    var timer by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(timer) {
        if (timer > 0) {
            delay(1000L)
            timer--
        } else {
            canResend = true
        }
    }

    LaunchedEffect(authState, phoneState) {
        if (authState is AdvancedAuthState.Success && !isLinking) {
            onNavigateToHome()
        }
        if (phoneState is PhoneAuthState.Verified && isLinking) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Verify OTP", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Verification Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Enter the 6-digit code sent to $phoneNumber",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                placeholder = "******",
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canResend) {
                    Text(
                        text = "Resend Code",
                        color = Color(0xFFF16B24),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            val currentState = phoneState
                            if (currentState is PhoneAuthState.CodeSent) {
                                viewModel.resendOtp(context as Activity, phoneNumber, currentState.resendToken)
                                timer = 60
                                canResend = false
                            }
                        }
                    )
                } else {
                    Text(
                        text = "Resend in ${timer}s",
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = if (phoneState is PhoneAuthState.Loading) "Verifying..." else "Verify",
                onClick = { 
                    val currentState = phoneState
                    if (currentState is PhoneAuthState.CodeSent && otpCode.length == 6) {
                        viewModel.verifyOtp(currentState.verificationId, otpCode, isLinking)
                    }
                },
                enabled = otpCode.length == 6 && phoneState !is PhoneAuthState.Loading,
                backgroundColor = Color(0xFFF16B24)
            )

            val currentState = phoneState
            if (currentState is PhoneAuthState.Error) {
                Text(
                    text = currentState.message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
