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

@Composable
fun VendorAnalyticsScreen() {
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
                RevenueOverviewCard()
            }

            // Top Meals
            item {
                Text("Best Selling Meals", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopMealItem("Doro Wat Special", "124 orders", 0.85f, Color(0xFFF16B24))
                    TopMealItem("Fasting Firfir", "98 orders", 0.65f, Color(0xFF4CAF50))
                    TopMealItem("Kitfo Premium", "76 orders", 0.50f, Color(0xFFE91E63))
                }
            }

            // Peak Times (Mock)
            item {
                PeakTimesCard()
            }
        }
    }
}

@Composable
fun RevenueOverviewCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Total Revenue (Monthly)", color = Color.Gray, fontSize = 14.sp)
            Text(
                text = "ETB 142,500",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                Text("12% vs last month", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Mock Chart Bar
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 1.0f).forEach { height ->
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
fun PeakTimesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Peak Ordering Times", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TimeSlot("12:00 - 14:00", "High Demand", Color(0xFFF16B24))
                TimeSlot("18:00 - 20:00", "Moderate", Color(0xFF2196F3))
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
