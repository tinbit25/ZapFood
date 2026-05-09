package com.example.food.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.OrderStatus
import com.example.food.data.model.OrderTimeline
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.OrderTrackingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderTrackingScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderTrackingViewModel = viewModel(),
    userViewModel: com.example.food.ui.viewmodel.UserViewModel = viewModel()
) {
    val timelineResource by viewModel.timelineState.collectAsState()
    val user by userViewModel.user.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.observeOrder(orderId)
    }

    // LEVEL 2 TEST — Real local notification trigger
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(timelineResource) {
        val resource = timelineResource
        if (resource is Resource.Success) {
            val timeline = resource.data
            if (timeline != null) {
                when (timeline.currentStatus) {
                    com.example.food.data.model.OrderStatus.ACCEPTED -> {
                        user?.userId?.let { uid ->
                            com.example.food.core.util.LocalNotificationHelper.showOrderNotification(
                                context,
                                uid,
                                "Order Accepted ✓",
                                "Your order is being prepared!"
                            )
                        }
                    }
                    com.example.food.data.model.OrderStatus.PREPARING -> {
                        user?.userId?.let { uid ->
                            com.example.food.core.util.LocalNotificationHelper.showOrderNotification(
                                context,
                                uid,
                                "Preparing Your Meal 🍳",
                                "The chef has started cooking your order."
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(title = "Track Order", onBackClick = onNavigateBack)
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val resource = timelineResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF16B24))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = resource.message ?: "Failed to load tracking data", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val timeline = resource.data
                    if (timeline != null) {
                        OrderTrackingContent(timeline)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTrackingContent(timeline: OrderTimeline) {
    val statuses = OrderStatus.values().filter { it != OrderStatus.CANCELLED }
    val currentStatusIndex = statuses.indexOf(timeline.currentStatus)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header Card
        TrackingHeaderCard(timeline)

        Spacer(modifier = Modifier.height(32.dp))

        // Animated Progress Bar (Horizontal)
        HorizontalProgressIndicator(currentStatusIndex, statuses.size)

        Spacer(modifier = Modifier.height(32.dp))

        // Vertical Timeline
        Text(
            text = "Order Journey",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(statuses) { index, status ->
                TimelineItem(
                    status = status,
                    isCompleted = index < currentStatusIndex,
                    isCurrent = index == currentStatusIndex,
                    isLast = index == statuses.size - 1,
                    history = timeline.history.find { it.status == status }
                )
            }
        }
    }
}

@Composable
fun TrackingHeaderCard(timeline: OrderTimeline) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Estimated Delivery", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "25 - 35 mins", // Mock for now, could use timeline.estimatedDeliveryTime
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF16B24).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = null,
                    tint = Color(0xFFF16B24),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun HorizontalProgressIndicator(currentIndex: Int, totalSteps: Int) {
    val progress = (currentIndex.toFloat() / (totalSteps - 1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF16B24), Color(0xFFFF9800))
                        )
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Order Placed", fontSize = 10.sp, color = Color.Gray)
            Text(text = "Enjoy!", fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TimelineItem(
    status: OrderStatus,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    history: com.example.food.data.model.OrderStatusHistory?
) {
    val color = when {
        isCompleted -> Color(0xFFF16B24)
        isCurrent -> Color(0xFFF16B24)
        else -> Color(0xFF2A2A2A)
    }

    val icon = when (status) {
        OrderStatus.PENDING -> Icons.Default.Receipt
        OrderStatus.ACCEPTED -> Icons.Default.CheckCircle
        OrderStatus.PREPARING -> Icons.Default.Restaurant
        OrderStatus.READY -> Icons.Default.Fastfood
        OrderStatus.ON_THE_WAY -> Icons.Default.DirectionsBike
        OrderStatus.DELIVERED -> Icons.Default.Check
        else -> Icons.Default.Info
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Timeline Connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isCurrent) Color.White else color)
                    .padding(if (isCurrent) 4.dp else 0.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent || isCompleted) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCurrent) color else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (isCompleted) Color(0xFFF16B24) else Color(0xFF2A2A2A))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = formatStatusName(status),
                fontSize = 16.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent || isCompleted) Color.White else Color.Gray
            )
            
            if (history != null) {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(history.timestamp))
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
            } else if (isCurrent) {
                Text(text = "In progress...", fontSize = 12.sp, color = Color(0xFFF16B24))
            }
        }
    }
}

private fun formatStatusName(status: OrderStatus): String {
    return status.name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }
}
