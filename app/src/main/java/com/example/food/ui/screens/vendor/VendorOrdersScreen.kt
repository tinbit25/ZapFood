package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VendorOrdersScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel
) {
    val user by userViewModel.user.collectAsState()
    val ordersState by orderViewModel.vendorOrders.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("New", "Preparing", "Ready", "Pickup", "Completed", "Cancelled")

    LaunchedEffect(user) {
        user?.let { orderViewModel.fetchVendorOrders(it.userId) }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF1A1A1A))) {
                com.example.food.ui.components.TopNavBar(title = "Order Management")
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFF16B24),
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFF16B24)
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val filteredOrders = remember(ordersState, selectedTab) {
            val orders = (ordersState as? Resource.Success)?.data ?: emptyList()
            when (selectedTab) {
                0 -> orders.filter { it.orderStatus == OrderStatus.PENDING || it.orderStatus == OrderStatus.BOOKED }
                1 -> orders.filter { it.orderStatus == OrderStatus.ACCEPTED || it.orderStatus == OrderStatus.PREPARING }
                2 -> orders.filter { it.orderStatus == OrderStatus.READY }
                3 -> orders.filter { it.orderStatus == OrderStatus.ON_THE_WAY || it.orderStatus == OrderStatus.ARRIVED }
                4 -> orders.filter { it.orderStatus == OrderStatus.DELIVERED }
                5 -> orders.filter { it.orderStatus == OrderStatus.CANCELLED }
                else -> orders
            }
        }

        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (ordersState is Resource.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFF16B24))
            } else if (filteredOrders.isEmpty()) {
                Text(
                    text = "No orders in this category",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        VendorProfessionalOrderCard(
                            order = order,
                            onUpdateStatus = { nextStatus ->
                                user?.let { orderViewModel.updateStatus(it, order.orderId, nextStatus) {} }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VendorProfessionalOrderCard(order: Order, onUpdateStatus: (OrderStatus) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Order #${order.orderId.take(6).uppercase()}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        OrderStatusBadge(order.orderStatus)
                    }
                    Text(text = order.customerName, fontSize = 12.sp, color = Color.Gray)
                }
                Text(text = dateFormat.format(Date(order.createdAt)), fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${item.quantity}x ${item.name}", fontSize = 14.sp, color = Color.LightGray)
                    Text(text = "ETB ${"%,.0f".format(item.price * item.quantity * 1000)}", fontSize = 14.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Total Amount", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "ETB ${"%,.0f".format(order.totalAmount * 1000)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF16B24)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Contextual Actions
                    when (order.orderStatus) {
                        OrderStatus.PENDING, OrderStatus.BOOKED -> {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Accept", fontSize = 12.sp)
                            }
                        }
                        OrderStatus.ACCEPTED -> {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Start Prep", fontSize = 12.sp)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = { onUpdateStatus(OrderStatus.READY) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mark Ready", fontSize = 12.sp)
                            }
                        }
                        OrderStatus.READY -> {
                           // Done side
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.PENDING, OrderStatus.BOOKED -> Color(0xFFF16B24)
        OrderStatus.ACCEPTED, OrderStatus.PREPARING -> Color(0xFF2196F3)
        OrderStatus.READY -> Color(0xFF4CAF50)
        OrderStatus.ARRIVED -> Color.Yellow
        OrderStatus.DELIVERED -> Color.Gray
        OrderStatus.CANCELLED -> Color.Red
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
