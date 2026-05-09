package com.example.food.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AdminViewModel

@Composable
fun AdminOrderMonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val dashboardData by viewModel.dashboardState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopNavBar(title = "Order Monitoring", onBackClick = onNavigateBack)

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Recent Platform Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (dashboardData) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFF16B24))
                        }
                    }
                    is Resource.Error -> {
                        Text(text = dashboardData.message ?: "Error loading orders", color = Color.Red)
                    }
                    is Resource.Success -> {
                        val orders = dashboardData.data?.recentOrders ?: emptyList()
                        if (orders.isEmpty()) {
                            EmptyState("No recent orders found")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(orders) { order ->
                                    AdminOrderCard(order)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = Color(0xFFF16B24), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Order #${order.orderId.takeLast(6)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OrderStatusBadge(order.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Customer ID", color = Color.Gray, fontSize = 10.sp)
                    Text(text = order.customerId.take(8), color = Color.White, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Amount", color = Color.Gray, fontSize = 10.sp)
                    Text(text = "RWF ${"%,.0f".format(order.totalAmount)}", color = Color(0xFFF16B24), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Placed on ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.createdAt))}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.ACCEPTED -> Color(0xFF2196F3)
        OrderStatus.PREPARING -> Color(0xFF9C27B0)
        OrderStatus.READY -> Color(0xFF4CAF50)
        OrderStatus.ON_THE_WAY -> Color(0xFF03A9F4)
        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color.Red
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
