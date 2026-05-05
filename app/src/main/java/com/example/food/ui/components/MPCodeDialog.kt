package com.example.food.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MPCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Enter MPCode", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column {
                Text(
                    text = "Enter a Meal Plan Code shared by a friend to access their custom plan.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CustomTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = "e.g. 1A2B3C"
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
                        color = Color(0xFFF16B24)
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Access Plan",
                onClick = { onConfirm(code) },
                enabled = code.isNotBlank() && !isLoading,
                modifier = Modifier.width(120.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(text = "Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1A1A1A)
    )
}
