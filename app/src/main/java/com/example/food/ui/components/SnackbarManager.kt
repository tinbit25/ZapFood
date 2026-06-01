package com.example.food.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    isError: Boolean = false
) {
    val backgroundColor = if (isError) Color(0xFFD32F2F) else Color(0xFF388E3C)
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isError) "Error" else "Success",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = snackbarData.visuals.message,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorSnackbar(snackbarData: SnackbarData) {
    CustomSnackbar(snackbarData = snackbarData, isError = true)
}

@Composable
fun SuccessSnackbar(snackbarData: SnackbarData) {
    CustomSnackbar(snackbarData = snackbarData, isError = false)
}

/**
 * A reusable SnackbarHost that styles snackbars automatically based on actionLabel.
 * If actionLabel is "ERROR", it renders an ErrorSnackbar. Otherwise, a SuccessSnackbar.
 */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(hostState = hostState) { data ->
        if (data.visuals.actionLabel == "ERROR") {
            ErrorSnackbar(snackbarData = data)
        } else if (data.visuals.actionLabel == "SUCCESS") {
            SuccessSnackbar(snackbarData = data)
        } else {
            // Fallback to default
            Snackbar(snackbarData = data)
        }
    }
}
