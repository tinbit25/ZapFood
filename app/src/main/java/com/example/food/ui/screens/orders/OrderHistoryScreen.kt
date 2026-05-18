package com.example.food.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
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
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.food.data.model.PaymentStatus
import com.example.food.data.model.PaymentMethod
import com.example.food.ui.viewmodel.PaymentViewModel
import com.example.food.ui.viewmodel.PaymentState

private val notifiedOrderStates = mutableSetOf<String>()

@Composable
fun OrderHistoryScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    paymentViewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFeedback: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val ordersState by orderViewModel.userOrders.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(user) {
        user?.let { orderViewModel.fetchUserOrders(it.userId) }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(ordersState) {
        val state = ordersState
        if (state is Resource.Success) {
            state.data?.firstOrNull()?.let { latestOrder ->
                val notificationKey = "${latestOrder.orderId}_${latestOrder.orderStatus.name}"
                if (!notifiedOrderStates.contains(notificationKey)) {
                    if (latestOrder.orderStatus == com.example.food.data.model.OrderStatus.ACCEPTED || 
                        latestOrder.orderStatus == com.example.food.data.model.OrderStatus.PREPARING) {
                        user?.userId?.let { uid ->
                            com.example.food.core.util.LocalNotificationHelper.showOrderNotification(
                                context,
                                uid,
                                "Order Update: ${latestOrder.orderStatus.name}",
                                "Order #${latestOrder.orderId.take(8)} is now ${latestOrder.orderStatus.name}"
                            )
                            notifiedOrderStates.add(notificationKey)
                        }
                    }
                }
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "Order History", onBackClick = onNavigateBack)

        when (val state = ordersState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message ?: "An error occurred", color = colorScheme.error)
                }
            }
            is Resource.Success -> {
                val orders = state.data ?: emptyList()
                if (orders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No orders found", color = colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(orders) { order ->
                            OrderHistoryCard(
                                order = order, 
                                dateFormat = dateFormat,
                                onCancel = {
                                    user?.let { u ->
                                        orderViewModel.cancelOrder(u, order.orderId) { /* Handle result */ }
                                    }
                                },
                                onRetryPayment = {
                                    user?.let { u ->
                                        paymentViewModel.retryPayment(
                                            orderId = order.orderId,
                                            userId = u.userId,
                                            amount = order.totalAmount,
                                            method = order.paymentMethod
                                        )
                                    }
                                },
                                onLeaveFeedback = {
                                    onNavigateToFeedback(order.orderId)
                                },
                                onNavigateToTracking = onNavigateToTracking
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: Order, 
    dateFormat: SimpleDateFormat, 
    onCancel: () -> Unit,
    onRetryPayment: () -> Unit,
    onLeaveFeedback: () -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order #${(order.orderId ?: "id").take(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = dateFormat.format(Date(order.createdAt)),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            order.items?.filterNotNull()?.forEach { item ->
                Text(
                    text = "${item.quantity}x ${item.name ?: "Item"}",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Total Amount", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                    Text(
                        text = "ETB ${order.totalAmount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PaymentBadge(status = order.paymentStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = order.orderStatus)
                }
            }

            if (order.paymentStatus == PaymentStatus.FAILED && order.orderStatus == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetryPayment,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry Payment", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (order.orderStatus == OrderStatus.PENDING) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel Order", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (order.orderStatus == OrderStatus.PENDING || order.orderStatus == OrderStatus.ACCEPTED || 
                order.orderStatus == OrderStatus.PREPARING || order.orderStatus == OrderStatus.READY || 
                order.orderStatus == OrderStatus.ON_THE_WAY) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigateToTracking(order.orderId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF).copy(alpha = 0.1f), contentColor = Color(0xFF00BFFF)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BFFF).copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Track Order", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (order.orderStatus == OrderStatus.DELIVERED) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLeaveFeedback,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32CD32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Leave Feedback", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PaymentBadge(status: PaymentStatus) {
    val color = when (status) {
        PaymentStatus.INITIATED, PaymentStatus.PROCESSING -> Color.Gray
        PaymentStatus.SUCCESS -> Color(0xFF32CD32)
        PaymentStatus.FAILED -> Color.Red
        PaymentStatus.REFUNDED -> Color(0xFF00BFFF)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = "PAY: ${status.name}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.INITIATED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_PROCESSING -> Color.Gray
        OrderStatus.PAID, OrderStatus.SENT_TO_VENDOR -> Color(0xFFE91E63)
        OrderStatus.PENDING, OrderStatus.BOOKED -> Color(0xFFFFA500)
        OrderStatus.ACCEPTED, OrderStatus.PREPARING -> Color(0xFF00BFFF)
        OrderStatus.READY, OrderStatus.ON_THE_WAY, OrderStatus.ARRIVED -> Color(0xFF32CD32)
        OrderStatus.DELIVERED -> Color(0xFF008000)
        OrderStatus.CANCELLED -> Color(0xFFFF0000)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
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
