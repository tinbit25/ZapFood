package com.example.food.ui.screens.admin

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
import kotlinx.coroutines.launch

@Composable
fun AdminControlCenterScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    var commissionRate by remember { mutableStateOf(15) }
    var showCommissionDialog by remember { mutableStateOf(false) }
    var isMaintenanceMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color(0xFF0A0A0A), // Premium Dark
        topBar = { TopNavBar(title = "Control Center") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Admin Profile Section
            item {
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
                                .background(Color(0xFF673AB7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user?.displayName?.take(1) ?: "A", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(user?.displayName ?: "Super Admin", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(user?.email ?: "admin@zapfood.com", color = Color.Gray, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Surface(color = Color(0xFF673AB7).copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    "ROOT ACCESS", 
                                    color = Color(0xFFB39DDB), 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // System Security & Logs
            item {
                SettingsSection("Security & Operations") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ControlCenterItem("Admin Activity Logs", Icons.Default.Security, "View all administrative actions") {
                        android.widget.Toast.makeText(context, "Logs restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ControlCenterItem("Abuse Reports", Icons.Default.ReportProblem, "2 unreviewed reports", textColor = Color.Red) {
                        android.widget.Toast.makeText(context, "Reports restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ControlCenterItem("Data Backup", Icons.Default.Backup, "Last backup: 2 hours ago") {
                        android.widget.Toast.makeText(context, "Backup restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Economics
            item {
                SettingsSection("Economics") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ControlCenterItem("Commission Rates", Icons.Default.Percent, "Current base rate: $commissionRate%") {
                        showCommissionDialog = true
                    }
                    ControlCenterItem("Vendor Payouts", Icons.Default.AccountBalanceWallet, "Review pending transfers") {
                        android.widget.Toast.makeText(context, "Payouts restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ControlCenterItem("Promotional Engine", Icons.Default.LocalOffer, "Manage active discounts") {
                        android.widget.Toast.makeText(context, "Promotions restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Platform Configuration
            item {
                SettingsSection("System Preferences") {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    ControlCenterItem("Notification Broadcasts", Icons.Default.Campaign, "Send push alerts to all users") {
                        android.widget.Toast.makeText(context, "Use dashboard broadcast", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ControlCenterItem("Geofencing Rules", Icons.Default.Map, "Active delivery zones") {
                        android.widget.Toast.makeText(context, "Geofencing restricted to production", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    ControlCenterItem(
                        "Maintenance Mode", 
                        if (isMaintenanceMode) Icons.Default.Build else Icons.Default.CheckCircle, 
                        if (isMaintenanceMode) "Currently offline" else "System is online",
                        textColor = if (isMaintenanceMode) Color.Red else Color.White
                    ) {
                        isMaintenanceMode = !isMaintenanceMode
                    }
                }
            }

            // Danger Zone
            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f), 
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Terminate Session", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showCommissionDialog) {
            var newRate by remember { mutableStateOf(commissionRate.toString()) }
            AlertDialog(
                onDismissRequest = { showCommissionDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Update Commission Rate", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newRate,
                        onValueChange = { newRate = it },
                        label = { Text("Rate (%)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF4CAF50),
                            cursorColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            newRate.toIntOrNull()?.let { 
                                commissionRate = it
                                scope.launch {
                                    snackbarHostState.showSnackbar("Commission rate updated to $it%")
                                }
                            }
                            showCommissionDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCommissionDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun ControlCenterItem(
    title: String, 
    icon: ImageVector, 
    subtitle: String? = null, 
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2A2A2A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}
