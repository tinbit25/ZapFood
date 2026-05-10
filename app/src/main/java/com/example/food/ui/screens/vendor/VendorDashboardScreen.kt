package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
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
import com.example.food.data.model.User
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@Composable
fun VendorDashboardScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    onLogout: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val ordersState by orderViewModel.vendorOrders.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        user?.let { orderViewModel.fetchVendorOrders(it.userId) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(
                title = "Vendor Portal",
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val state = ordersState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF16B24))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "Error loading orders", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val orders = state.data ?: emptyList()
                    if (orders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "No incoming orders", color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            com.example.food.data.repository.MealRepository().seedMealsForVendor(user!!.userId, user!!.displayName ?: "My Shop")
                                            snackbarHostState.showSnackbar("Meals seeded! Restart app to see them.")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24))
                                ) {
                                    Text("Seed My Meals")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(orders) { order ->
                                VendorOrderCard(
                                    order = order,
                                    user = user!!,
                                    onUpdateStatus = { nextStatus ->
                                        orderViewModel.updateStatus(user!!, order.orderId, nextStatus) { result ->
                                            if (result is Resource.Error) {
                                                // Handle error (e.g. snackbar)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorOrderCard(order: Order, user: User, onUpdateStatus: (OrderStatus) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Order #${order.orderId.take(6).uppercase()}", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = order.customerName, fontSize = 12.sp, color = Color.Gray)
                }
                Text(text = dateFormat.format(Date(order.createdAt)), fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            order.items.forEach { item ->
                Text(text = "${item.quantity}x ${item.name}", fontSize = 14.sp, color = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ETB ${"%,.0f".format(order.totalAmount * 1000)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF16B24)
                )
                
                VendorStatusActions(status = order.status, onUpdateStatus = onUpdateStatus)
            }
        }
    }
}

@Composable
fun VendorStatusActions(status: OrderStatus, onUpdateStatus: (OrderStatus) -> Unit) {
    when (status) {
        OrderStatus.PENDING -> {
            Row {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Reject", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onUpdateStatus(OrderStatus.ACCEPTED) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Accept", fontSize = 12.sp)
                }
            }
        }
        OrderStatus.ACCEPTED -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.PREPARING) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Start Preparing", fontSize = 12.sp)
            }
        }
        OrderStatus.PREPARING -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.READY) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32CD32)),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Mark Ready", fontSize = 12.sp)
            }
        }
        OrderStatus.READY -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.ON_THE_WAY) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Send for Delivery", fontSize = 12.sp)
            }
        }
        OrderStatus.ON_THE_WAY -> {
            Button(
                onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000)),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Mark Delivered", fontSize = 12.sp)
            }
        }
        else -> {
            Text(text = status.name, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
