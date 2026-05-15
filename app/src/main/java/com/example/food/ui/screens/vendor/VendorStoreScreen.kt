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

@Composable
fun VendorStoreScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    
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
                StoreProfileHeader(user?.businessName ?: "My Business", user?.email ?: "")
            }

            // Status Section
            item {
                StoreStatusCard()
            }

            // Settings Groups
            item {
                SettingsGroup("Business Profile") {
                    SettingsItem("Business Info", Icons.Default.Business)
                    SettingsItem("Operating Hours", Icons.Default.Schedule)
                    SettingsItem("Delivery Radius", Icons.Default.Map)
                }
            }

            item {
                SettingsGroup("Operations") {
                    SettingsItem("Payout Settings", Icons.Default.AccountBalanceWallet)
                    SettingsItem("Tax Documents", Icons.Default.Description)
                    SettingsItem("Staff Management", Icons.Default.Badge)
                }
            }

            item {
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
                Text(name.take(1), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
fun StoreStatusCard() {
    var isOnline by remember { mutableStateOf(true) }
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
                Text("Customers can place orders now", color = Color.Gray, fontSize = 12.sp)
            }
            Switch(
                checked = isOnline,
                onCheckedChange = { isOnline = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF4CAF50),
                    checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 14.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}
