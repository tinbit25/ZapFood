package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.VendorStateManager

@Composable
fun VendorStoreScreen(
    userViewModel: UserViewModel,
    vendorStateManager: VendorStateManager,
    onLogout: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val vendor by vendorStateManager.vendor.collectAsState()
    
    val isStoreOpen = vendor?.isActive == true
    
    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = { TopNavBar(title = "Store Settings") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Header
            item {
                val displayName = vendor?.businessName ?: user?.displayName ?: "My Business"
                val displayEmail = user?.email ?: ""
                StoreProfileHeader(displayName, displayEmail)
            }

            // Status Section
            item {
                StoreStatusCard(
                    isOnline = isStoreOpen,
                    onToggle = { vendorStateManager.toggleActiveStatus(it) }
                )
            }

            // Logout Button
            item {
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Logout from Portal")
                }
            }
        }
    }
}

@Composable
fun StoreProfileHeader(name: String, email: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF16B24)),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (name.isNotEmpty()) name.take(1).uppercase() else "V"
                Text(initial, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(email, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StoreStatusCard(
    isOnline: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOnline) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isOnline) "Store is Online" else "Store is Offline",
                    color = if (isOnline) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOnline) "Customers can place orders now" else "You are not receiving new orders",
                    color = Color.Gray, 
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = isOnline,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4CAF50),
                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                )
            )
        }
    }
}
