package com.example.food.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val onboardingDataStore = remember { com.example.food.data.datastore.OnboardingDataStore(context) }
    
    LaunchedEffect(key1 = true) {
        delay(2000L) // 2 second delay for splash
        
        onboardingDataStore.readOnboardingState().collect { completed ->
            if (!completed) {
                onNavigateToOnboarding()
            } else if (FirebaseAuth.getInstance().currentUser != null) {
                onNavigateToHome()
            } else {
                onNavigateToWelcome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder for Logo, using stylized text for now
        Text(
            text = "ZapFood",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}
