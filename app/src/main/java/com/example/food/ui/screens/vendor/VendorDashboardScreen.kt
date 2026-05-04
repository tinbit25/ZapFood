package com.example.food.ui.screens.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.TopNavBar

@Composable
fun VendorDashboardScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "Vendor Dashboard")
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Vendor Portal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Manage your meal plans and orders here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}
