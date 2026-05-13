package com.example.food.ui.screens.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.viewmodel.VendorUIState

@Composable
fun VendorVerificationStateScreen(
    state: VendorUIState,
    onNavigateBack: () -> Unit,
    onContactSupport: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val (icon, title, message, color) = when (state) {
                VendorUIState.PendingReview -> Quad(
                    Icons.Default.HourglassEmpty,
                    "Application Received",
                    "We have received your business profile. Our team is currently reviewing your documents. This usually takes 24-48 hours.",
                    MaterialTheme.colorScheme.primary
                )
                VendorUIState.Verifying -> Quad(
                    Icons.Default.VerifiedUser,
                    "Verification in Progress",
                    "We are currently verifying your credentials. You will be notified once your account is activated.",
                    Color(0xFF2196F3)
                )
                VendorUIState.Suspended -> Quad(
                    Icons.Default.Block,
                    "Account Suspended",
                    "Your vendor account has been suspended due to a policy violation. Please contact support for more information.",
                    MaterialTheme.colorScheme.error
                )
                VendorUIState.Rejected -> Quad(
                    Icons.Default.ErrorOutline,
                    "Application Rejected",
                    "Unfortunately, your application was not approved at this time. Please check your email for detailed feedback.",
                    MaterialTheme.colorScheme.error
                )
                else -> Quad(
                    Icons.Default.Info,
                    "Status Update",
                    "Your account status is being updated.",
                    MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = color
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onContactSupport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Contact Support")
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Go Back to Profile")
            }
        }
    }
}

private data class Quad(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val color: Color
)
