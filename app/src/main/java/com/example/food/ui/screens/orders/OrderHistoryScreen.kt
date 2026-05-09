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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun OrderHistoryScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    paymentViewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFeedback: (String) -> Unit,
    onNavigateToTracking: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text(text = "Absolute Minimal Screen", color = Color.White)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Order #${(order.orderId ?: "id").take(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = dateFormat.format(Date(order.createdAt)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            order.items?.filterNotNull()?.forEach { item ->
                Text(
                    text = "${item.quantity}x ${item.name ?: "Item"}",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Total Amount", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "ETB ${order.totalAmount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF16B24)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = order.status)
                }
            }

            // Temporarily removed buttons for debugging
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
        OrderStatus.PENDING -> Color(0xFFFFA500)
        OrderStatus.ACCEPTED, OrderStatus.PREPARING -> Color(0xFF00BFFF)
        OrderStatus.READY, OrderStatus.ON_THE_WAY -> Color(0xFF32CD32)
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
