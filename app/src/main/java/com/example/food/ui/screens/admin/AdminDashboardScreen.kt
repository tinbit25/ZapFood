package com.example.food.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToVendors: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val dashboardData by viewModel.dashboardState.collectAsState()
    val health by viewModel.systemHealth.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopNavBar(title = "Admin Console", onBackClick = onNavigateBack)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Platform Overview",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Real-time insights and system control",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Revenue",
                        value = "RWF ${if (dashboardData is Resource.Success) "%,.0f".format(dashboardData.data?.totalRevenue ?: 0.0) else "--"}",
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF4CAF50)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Orders",
                        value = "${if (dashboardData is Resource.Success) dashboardData.data?.totalOrders ?: 0 else "--"}",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Users",
                        value = "${if (dashboardData is Resource.Success) dashboardData.data?.activeUsers ?: 0 else "--"}",
                        icon = Icons.Default.People,
                        color = Color(0xFFFF9800)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Vendors",
                        value = "${if (dashboardData is Resource.Success) dashboardData.data?.pendingVendors ?: 0 else "--"}",
                        icon = Icons.Default.Store,
                        color = Color(0xFFE91E63),
                        onClick = onNavigateToVendors
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Management Quick Links
                Text("Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                QuickLinkItem(Icons.Default.Group, "User Management", "Verify, activate, or suspend users", onNavigateToUsers)
                QuickLinkItem(Icons.Default.Storefront, "Vendor Approvals", "Review and approve new vendor partners", onNavigateToVendors)
                QuickLinkItem(Icons.Default.History, "Order Monitoring", "Track all active and past transactions", onNavigateToOrders)

                Spacer(modifier = Modifier.height(32.dp))

                // System Health
                Text("System Health", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                HealthCard(health)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun QuickLinkItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF16B24).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFF16B24))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun HealthCard(health: SystemHealth) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Operational Status", color = Color.White, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (health.errorRate < 1.0) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (health.errorRate < 1.0) "HEALTHY" else "WARNING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (health.errorRate < 1.0) Color(0xFF4CAF50) else Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthMetric("Latency", "${health.apiResponseTime}ms")
                HealthMetric("Error Rate", "${"%.1f".format(health.errorRate)}%")
                HealthMetric("Failures", "${health.failedPaymentsCount}")
            }
        }
    }
}

@Composable
fun HealthMetric(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
