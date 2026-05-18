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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.ui.components.TopNavBar

@Composable
fun VendorDashboardScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToAddMeal: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val ordersState by orderViewModel.vendorOrders.collectAsState()
    
    var isStoreOpen by remember { mutableStateOf(true) }

    LaunchedEffect(user) {
        user?.let { orderViewModel.fetchVendorOrders(it.userId) }
    }

    // Payment gate: only show orders that have been paid or are cash orders
    val paidOrdersState = when (val state = ordersState) {
        is com.example.food.core.util.Resource.Success -> {
            val paidOrders = state.data?.filter { order ->
                order.paymentStatus == com.example.food.data.model.PaymentStatus.SUCCESS ||
                order.paymentMethod == com.example.food.data.model.PaymentMethod.CASH
            } ?: emptyList()
            com.example.food.core.util.Resource.Success(paidOrders)
        }
        else -> state
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF1A1A1A))) {
                TopNavBar(
                    title = "Command Center",
                    actions = {
                        Switch(
                            checked = isStoreOpen,
                            onCheckedChange = { isStoreOpen = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF4CAF50),
                                checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isStoreOpen) "OPEN" else "CLOSED",
                            color = if (isStoreOpen) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = Color(0xFFF16B24),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Pickup")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Business Metrics Row
            item {
                BusinessMetricsGrid(paidOrdersState)
            }

            // Quick Actions
            item {
                Text("Quick Actions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard("Add Meal", Icons.Default.Add, Color(0xFFF16B24), Modifier.weight(1f), onNavigateToAddMeal)
                    QuickActionCard("Update Stock", Icons.Default.Inventory, Color(0xFF2196F3), Modifier.weight(1f), onNavigateToAddMeal)
                }
            }

            // Performance Insights (Mini)
            item {
                PerformanceInsightCard()
            }

            // Urgent Notifications / Pending
            item {
                UrgentOrdersList(paidOrdersState)
            }
        }
    }
}

@Composable
fun BusinessMetricsGrid(ordersState: Resource<List<com.example.food.data.model.Order>>) {
    val orders = (ordersState as? Resource.Success)?.data ?: emptyList()
    val activeOrders = orders.count {
        it.orderStatus != com.example.food.data.model.OrderStatus.DELIVERED &&
        it.orderStatus != com.example.food.data.model.OrderStatus.CANCELLED
    }
    // Revenue = sum of actual paid order amounts (no mock multiplier)
    val todayRevenue = orders
        .filter { it.paymentStatus == com.example.food.data.model.PaymentStatus.SUCCESS ||
                  it.paymentMethod == com.example.food.data.model.PaymentMethod.CASH }
        .sumOf { it.totalAmount }
    val pendingPickups = orders.count { it.orderStatus == com.example.food.data.model.OrderStatus.READY &&
        it.orderType == com.example.food.data.model.OrderType.TAKEAWAY }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                title = "Active Orders",
                value = activeOrders.toString(),
                icon = Icons.Default.Assignment,
                color = Color(0xFFF16B24),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Today's Revenue",
                value = "ETB ${"%,.0f".format(todayRevenue)}",
                icon = Icons.Default.Payments,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
        // Second row: pending pickups
        if (pendingPickups > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF16B24).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFFF16B24))
                        Spacer(Modifier.width(12.dp))
                        Text("$pendingPickups order(s) awaiting QR pickup scan", color = Color.White, fontSize = 13.sp)
                    }
                    Surface(
                        color = Color(0xFFF16B24).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "SCAN NOW",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color(0xFFF16B24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun PerformanceInsightCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text("Performance Insight", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your \"Special Doro Wat Combo\" is trending! Increase stock for the weekend rush.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UrgentOrdersList(ordersState: Resource<List<com.example.food.data.model.Order>>) {
    val orders = (ordersState as? Resource.Success)?.data?.filter { 
        it.orderStatus == com.example.food.data.model.OrderStatus.READY || it.orderStatus == com.example.food.data.model.OrderStatus.ARRIVED 
    } ?: emptyList()

    Column {
        Text("Urgent Attention", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        if (orders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "All caught up! No urgent orders.",
                    modifier = Modifier.padding(24.dp),
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            orders.forEach { order ->
                UrgentOrderCard(order)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun UrgentOrderCard(order: com.example.food.data.model.Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (order.orderStatus == com.example.food.data.model.OrderStatus.ARRIVED) Color.Yellow.copy(alpha = 0.1f)
                            else Color(0xFF9C27B0).copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (order.orderStatus == com.example.food.data.model.OrderStatus.ARRIVED) Icons.Default.TableBar else Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = if (order.orderStatus == com.example.food.data.model.OrderStatus.ARRIVED) Color.Yellow else Color(0xFF9C27B0)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = order.customerName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (order.orderStatus == com.example.food.data.model.OrderStatus.ARRIVED) "Waiting at Table ${order.dineInInfo?.tableNumber}" else "Awaiting Pickup",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
