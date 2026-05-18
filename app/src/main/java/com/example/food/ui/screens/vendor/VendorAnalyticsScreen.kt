package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.Order
import com.example.food.data.model.OrderStatus
import com.example.food.data.model.PaymentStatus
import com.example.food.data.model.PaymentMethod
import java.util.Calendar

@Composable
fun VendorAnalyticsScreen(orderViewModel: OrderViewModel) {
    val ordersState by orderViewModel.vendorOrders.collectAsState()

    val orders = (ordersState as? Resource.Success)?.data ?: emptyList()

    // Filter valid orders for analytics
    val validOrders = orders.filter {
        it.orderStatus != OrderStatus.CANCELLED &&
        (it.paymentStatus == PaymentStatus.SUCCESS || it.paymentMethod == PaymentMethod.CASH)
    }

    // Calculations
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    calendar.add(Calendar.MONTH, -1)
    val lastMonth = calendar.get(Calendar.MONTH)
    val lastMonthYear = calendar.get(Calendar.YEAR)

    var currentMonthRevenue = 0.0
    var lastMonthRevenue = 0.0

    // For 7-day chart
    val last7DaysRevenue = DoubleArray(7) { 0.0 }
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Peak times
    val hourCounts = IntArray(24) { 0 }

    // Best selling
    val itemCounts = mutableMapOf<String, Int>()

    validOrders.forEach { order ->
        val cal = Calendar.getInstance().apply { timeInMillis = order.createdAt }
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)

        if (month == currentMonth && year == currentYear) {
            currentMonthRevenue += order.totalAmount
        } else if (month == lastMonth && year == lastMonthYear) {
            lastMonthRevenue += order.totalAmount
        }

        val daysAgo = ((todayStart - order.createdAt) / (1000 * 60 * 60 * 24)).toInt()
        if (daysAgo in 0..6) {
            // Index 6 is today, 0 is 6 days ago
            last7DaysRevenue[6 - daysAgo] += order.totalAmount
        }

        // Count hours
        hourCounts[cal.get(Calendar.HOUR_OF_DAY)]++

        // Count items
        order.items.forEach { item ->
            itemCounts[item.name] = (itemCounts[item.name] ?: 0) + item.quantity
        }
    }

    val growthPercentage = if (lastMonthRevenue > 0) {
        ((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100
    } else if (currentMonthRevenue > 0) {
        100.0
    } else {
        0.0
    }

    val maxChartRevenue = last7DaysRevenue.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val chartFractions = last7DaysRevenue.map { (it / maxChartRevenue).toFloat().coerceAtLeast(0.05f) }

    val topItems = itemCounts.entries.sortedByDescending { it.value }.take(3)
    val maxItemCount = topItems.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1

    val sortedHours = hourCounts.mapIndexed { index, count -> index to count }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

    val topHour1 = sortedHours.getOrNull(0)
    val topHour2 = sortedHours.getOrNull(1)

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = { TopNavBar(title = "Business Insights") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Revenue Overview
            item {
                RevenueOverviewCard(currentMonthRevenue, growthPercentage, chartFractions)
            }

            // Top Meals
            item {
                Text("Best Selling Meals", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                if (topItems.isEmpty()) {
                    Text("No sales data available yet.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    val colors = listOf(Color(0xFFF16B24), Color(0xFF4CAF50), Color(0xFFE91E63))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        topItems.forEachIndexed { index, entry ->
                            TopMealItem(
                                name = entry.key,
                                orders = "${entry.value} orders",
                                percentage = (entry.value.toFloat() / maxItemCount).coerceIn(0.1f, 1f),
                                color = colors.getOrElse(index) { Color.Gray }
                            )
                        }
                    }
                }
            }

            // Peak Times
            item {
                PeakTimesCard(topHour1, topHour2)
            }
        }
    }
}

@Composable
fun RevenueOverviewCard(revenue: Double, growth: Double, chartFractions: List<Float>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Total Revenue (Monthly)", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "ETB ${"%,.0f".format(revenue)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPositive = growth >= 0
                val color = if (isPositive) Color(0xFF4CAF50) else Color.Red
                val icon = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Text("${"%.1f".format(kotlin.math.abs(growth))}% vs last month", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
            
            // 7-day Chart Bar
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                chartFractions.forEach { height ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(height)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFF16B24), Color(0xFFF16B24).copy(alpha = 0.3f))),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun TopMealItem(name: String, orders: String, percentage: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = Color.White, fontSize = 14.sp)
            Text(orders, color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFF2A2A2A), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun PeakTimesCard(topHour1: Pair<Int, Int>?, topHour2: Pair<Int, Int>?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Peak Ordering Times", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            if (topHour1 == null) {
                Text("Not enough data to determine peak times.", color = Color.Gray, fontSize = 14.sp)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val formatHour = { h: Int -> "${h.toString().padStart(2, '0')}:00 - ${(h + 1).toString().padStart(2, '0')}:00" }
                    TimeSlot(formatHour(topHour1.first), "High Demand", Color(0xFFF16B24))
                    if (topHour2 != null) {
                        TimeSlot(formatHour(topHour2.first), "Moderate", Color(0xFF2196F3))
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlot(time: String, label: String, color: Color) {
    Column {
        Text(time, color = Color.Gray, fontSize = 12.sp)
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
