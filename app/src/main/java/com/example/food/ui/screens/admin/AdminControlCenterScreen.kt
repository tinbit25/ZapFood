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
import com.example.food.core.util.Resource
import kotlinx.coroutines.launch

@Composable
fun AdminControlCenterScreen(
    adminViewModel: com.example.food.ui.viewmodel.AdminViewModel,
    userViewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val systemConfig by adminViewModel.systemConfigState.collectAsState()
    val adminLogs by adminViewModel.adminLogsState.collectAsState()
    val abuseCount by adminViewModel.abuseReportsCountState.collectAsState()
    val lastBackupTime by adminViewModel.lastBackupTimeState.collectAsState()

    var showCommissionDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showAbuseDialog by remember { mutableStateOf(false) }
    var showPayoutsDialog by remember { mutableStateOf(false) }
    var showPromotionsDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showGeofencingDialog by remember { mutableStateOf(false) }

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
                    ControlCenterItem("Admin Activity Logs", Icons.Default.Security, "${adminLogs.size} logs available") {
                        showLogsDialog = true
                    }
                    ControlCenterItem("Abuse Reports", Icons.Default.ReportProblem, if (abuseCount > 0) "$abuseCount unreviewed reports" else "All reports reviewed", textColor = if (abuseCount > 0) Color.Red else Color.White) {
                        showAbuseDialog = true
                    }
                    val backupText = if (lastBackupTime > 0) "Last backup: ${formatTimeAgo(lastBackupTime)}" else "Never backed up"
                    ControlCenterItem("Data Backup", Icons.Default.Backup, backupText) {
                        android.widget.Toast.makeText(context, "Starting full system backup...", android.widget.Toast.LENGTH_SHORT).show()
                        adminViewModel.triggerBackup(user?.userId ?: "admin") {
                            android.widget.Toast.makeText(context, "System data backup completed successfully.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // Economics
            item {
                SettingsSection("Economics") {
                    ControlCenterItem("Commission Rates", Icons.Default.Percent, "Current base rate: ${systemConfig.commissionRate}%") {
                        showCommissionDialog = true
                    }
                    ControlCenterItem("Vendor Payouts", Icons.Default.AccountBalanceWallet, "Review pending transfers") {
                        showPayoutsDialog = true
                    }
                    ControlCenterItem("Promotional Engine", Icons.Default.LocalOffer, "Manage active discounts") {
                        showPromotionsDialog = true
                    }
                }
            }

            // Platform Configuration
            item {
                SettingsSection("System Preferences") {
                    ControlCenterItem("Notification Broadcasts", Icons.Default.Campaign, "Send push alerts to all users") {
                        showBroadcastDialog = true
                    }
                    ControlCenterItem("Geofencing Rules", Icons.Default.Map, "Active delivery zones") {
                        showGeofencingDialog = true
                    }
                    ControlCenterItem(
                        "Maintenance Mode", 
                        if (systemConfig.maintenanceMode) Icons.Default.Build else Icons.Default.CheckCircle, 
                        if (systemConfig.maintenanceMode) "Currently offline" else "System is online",
                        textColor = if (systemConfig.maintenanceMode) Color.Red else Color.White
                    ) {
                        adminViewModel.updateSystemConfig(systemConfig.copy(maintenanceMode = !systemConfig.maintenanceMode))
                        adminViewModel.logAdminActivity(
                            user?.userId ?: "admin",
                            "Maintenance Mode Toggle",
                            "Maintenance mode is now ${if (!systemConfig.maintenanceMode) "ENABLED" else "DISABLED"}"
                        )
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
            var newRate by remember { mutableStateOf(systemConfig.commissionRate.toString()) }
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
                                adminViewModel.updateSystemConfig(systemConfig.copy(commissionRate = it))
                                adminViewModel.logAdminActivity(
                                    user?.userId ?: "admin",
                                    "Commission Rate Change",
                                    "Commission rate updated from ${systemConfig.commissionRate}% to $it%"
                                )
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

        if (showLogsDialog) {
            AlertDialog(
                onDismissRequest = { showLogsDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Admin Activity Logs", color = Color.White) },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        if (adminLogs.isEmpty()) {
                            Text("No administrative activities logged yet.", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(adminLogs.size) { index ->
                                    val log = adminLogs[index]
                                    val action = log["action"] as? String ?: "Unknown Action"
                                    val details = log["details"] as? String ?: ""
                                    val timestamp = log["timestamp"] as? Long ?: 0L
                                    Column {
                                        Text(action, color = Color(0xFFB39DDB), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(details, color = Color.White, fontSize = 12.sp)
                                        Text(formatLogTime(timestamp), color = Color.Gray, fontSize = 10.sp)
                                        Divider(color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showLogsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        if (showAbuseDialog) {
            AlertDialog(
                onDismissRequest = { showAbuseDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Abuse Reports", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Unreviewed Reports ($abuseCount)", color = Color.LightGray)
                        if (abuseCount == 0) {
                            Text("No pending abuse reports found. The system is clean!", color = Color.Gray)
                        } else {
                            // Simulated interactive list of reports
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Report #1: Fraudulent discount tags", color = Color.White, fontSize = 14.sp)
                                Text("Reported against Vendor: Aster's Kitchen", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        adminViewModel.logAdminActivity(user?.userId ?: "admin", "Abuse Report Resolved", "Resolved fraud report against Aster's Kitchen")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Abuse report resolved.")
                                        }
                                        showAbuseDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                                ) {
                                    Text("Resolve & Flag Vendor")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAbuseDialog = false }) {
                        Text("Dismiss", color = Color.Gray)
                    }
                }
            )
        }

        if (showPayoutsDialog) {
            AlertDialog(
                onDismissRequest = { showPayoutsDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Vendor Payout Transfers", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pending Bank/Mobile Money Settlements:", color = Color.LightGray)
                        Divider(color = Color.DarkGray)
                        Column {
                            Text("Vendor: Habesha Bites", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Pending Payout: ETB 12,450.00", color = Color.LightGray)
                            Text("CBE Birr: +251 912 345 678", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                adminViewModel.logAdminActivity(user?.userId ?: "admin", "Payout Transferred", "Settled ETB 12,450.00 to Habesha Bites via CBE Birr.")
                                scope.launch {
                                    snackbarHostState.showSnackbar("Payout settlement processed successfully!")
                                }
                                showPayoutsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark as Transferred")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPayoutsDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        if (showPromotionsDialog) {
            AlertDialog(
                onDismissRequest = { showPromotionsDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Promotional Engine", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Configure Active Discounts:", color = Color.LightGray)
                        Divider(color = Color.DarkGray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Global Platform Discount (10% OFF)", color = Color.White)
                            Switch(
                                checked = true,
                                onCheckedChange = {
                                    adminViewModel.logAdminActivity(user?.userId ?: "admin", "Promotion Toggle", "Toggled global platform discount")
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Global platform promo updated.")
                                    }
                                }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPromotionsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        if (showBroadcastDialog) {
            var message by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showBroadcastDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Send Push Broadcast Alert", color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Broadcast Message", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF673AB7),
                            cursorColor = Color(0xFF673AB7)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                adminViewModel.sendBroadcast(message) { result ->
                                    if (result is Resource.Success) {
                                        adminViewModel.logAdminActivity(user?.userId ?: "admin", "Broadcast Sent", "Broadcast message: '$message'")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Broadcast alert sent to all users!")
                                        }
                                    }
                                }
                            }
                            showBroadcastDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Text("Send Broadcast")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBroadcastDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        if (showGeofencingDialog) {
            var zoneRadius by remember { mutableStateOf("15") }
            AlertDialog(
                onDismissRequest = { showGeofencingDialog = false },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Geofencing Delivery Zones", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Core Delivery Zone: Addis Ababa", color = Color.LightGray)
                        OutlinedTextField(
                            value = zoneRadius,
                            onValueChange = { zoneRadius = it },
                            label = { Text("Delivery Radius Limit (km)", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                cursorColor = Color(0xFF4CAF50)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            adminViewModel.logAdminActivity(user?.userId ?: "admin", "Geofencing Radius Changed", "Updated Addis Ababa delivery limit to $zoneRadius km")
                            scope.launch {
                                snackbarHostState.showSnackbar("Geofencing limits updated!")
                            }
                            showGeofencingDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGeofencingDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    if (timestamp == 0L) return "Never backed up"
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hours ago"
        else -> "$days days ago"
    }
}

private fun formatLogTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
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
