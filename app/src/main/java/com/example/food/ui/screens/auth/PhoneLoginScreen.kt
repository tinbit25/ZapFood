package com.example.food.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
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

@Composable
fun PhoneLoginScreen(
    onNavigateToOTP: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    isLinking: Boolean = false,
    viewModel: AuthViewModel = viewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    val phoneState by viewModel.phoneAuthState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(phoneState) {
        if (phoneState is PhoneAuthState.CodeSent) {
            onNavigateToOTP(phoneNumber, isLinking)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = if (isLinking) "Link Phone" else "Phone Login", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (isLinking) "Link Your Number" else "Enter Phone Number",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "We'll send a 6-digit verification code",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "09xxxxxxxx or 07xxxxxxxx",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = if (phoneState is PhoneAuthState.Loading) "Sending..." else "Send Code",
                onClick = { 
                    if (phoneNumber.isNotEmpty()) {
                        viewModel.sendOtp(context as Activity, phoneNumber)
                    }
                },
                enabled = phoneNumber.length >= 9 && phoneState !is PhoneAuthState.Loading,
                backgroundColor = Color(0xFFF16B24)
            )

            if (phoneState is PhoneAuthState.Error) {
                Text(
                    text = (phoneState as PhoneAuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}
