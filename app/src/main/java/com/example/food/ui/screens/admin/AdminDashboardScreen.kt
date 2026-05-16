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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit,
    onNavigateToVendors: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: AdminViewModel = viewModel(),
    mealViewModel: com.example.food.ui.viewmodel.MealViewModel = viewModel(),
    mealPlanViewModel: com.example.food.ui.viewmodel.MealPlanViewModel = viewModel()
) {
    val dashboardData by viewModel.dashboardState.collectAsState()
    val health by viewModel.systemHealth.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers(role = UserRole.VENDOR)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // System Overview Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("System Overview", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (health.errorRate < 1.0) Color(0xFF4CAF50) else Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (health.errorRate < 1.0) "All Systems Operational" else "System Warning", 
                                color = Color.Gray, 
                                fontSize = 14.sp
                            )
                        }
                    }
                    IconButton(onClick = { /* TODO Notifications */ }) {
                        BadgedBox(
                            badge = { Badge(containerColor = Color.Red) { Text("2") } }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            // Quick Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionChip("Approvals", Icons.Default.CheckCircle, Color(0xFFE91E63), onNavigateToVendors, Modifier.weight(1f))
                    QuickActionChip("Broadcast", Icons.Default.Campaign, Color(0xFF2196F3), {}, Modifier.weight(1f))
                    QuickActionChip("Users", Icons.Default.People, Color(0xFFFF9800), onNavigateToUsers, Modifier.weight(1f))
                }
            }

            // Vendor Operations Card
            item {
                DashboardSectionCard(
                    title = "Vendor Operations",
                    icon = Icons.Default.Storefront,
                    color = Color(0xFFE91E63),
                    onClick = onNavigateToVendors
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Pending", "${if (dashboardData is Resource.Success) dashboardData.data?.pendingVendors ?: 0 else "--"}", Color(0xFFE91E63))
                        MetricItem("Active", "142", Color.White)
                        MetricItem("Suspended", "3", Color.Red)
                    }
                }
            }

            // Order Operations Card
            item {
                DashboardSectionCard(
                    title = "Order Monitoring",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFF2196F3),
                    onClick = onNavigateToOrders
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Live", "28", Color(0xFF2196F3))
                        MetricItem("Total Today", "${if (dashboardData is Resource.Success) dashboardData.data?.totalOrders ?: 0 else "--"}", Color.White)
                        MetricItem("Failed", "1", Color.Red)
                    }
                }
            }

            // Revenue Engine Card
            item {
                DashboardSectionCard(
                    title = "Revenue Engine",
                    icon = Icons.Default.MonetizationOn,
                    color = Color(0xFF4CAF50),
                    onClick = {}
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Today", "ETB ${if (dashboardData is Resource.Success) "%,.0f".format(dashboardData.data?.totalRevenue ?: 0.0) else "--"}", Color(0xFF4CAF50))
                        MetricItem("Commission", "ETB 45K", Color.White)
                        MetricItem("Pending Payout", "ETB 12K", Color.Gray)
                    }
                }
            }

            // Seed Data Action (Kept from original)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch { snackbarHostState.showSnackbar("Seeding disabled in Control Center demo.") }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A1A1A)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Seed System Data", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuickActionChip(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardSectionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}
