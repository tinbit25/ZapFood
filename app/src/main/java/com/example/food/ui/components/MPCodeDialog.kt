package com.example.food.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MPCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Enter MPCode", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Enter a Meal Plan Code shared by a friend to access their custom plan.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CustomTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = "e.g. KRAV-1234"
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Access Plan",
                onClick = { onConfirm(code) },
                enabled = code.isNotBlank(),
                modifier = Modifier.width(120.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
