package com.example.food.ui.screens.admin

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.ui.viewmodel.AdminViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.TopNavBar
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun AdminAnalyticsScreen(
    viewModel: AdminViewModel = viewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    
    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = { TopNavBar(title = "Platform Analytics") }
    ) { padding ->
        when (val state = dashboardState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFF16B24))
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message ?: "Failed to load analytics", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val data = state.data!!
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Text("Revenue Trajectory (Last 7 Days)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        RealLineChart(data.revenueByDay)
                    }

                    item {
                        val avgOrder = if (data.totalOrders > 0) data.totalRevenue / data.totalOrders else 0.0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            MiniStatCard("Avg Order Value", "ETB ${"%.0f".format(avgOrder)}", "+12%", Modifier.weight(1f))
                            MiniStatCard("Active Users", "${data.activeUsers}", "+5%", Modifier.weight(1f))
                        }
                    }

                    item {
                        Text("Peak Ordering Times", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        RealBarChart(data.hourlyDistribution)
                    }

                    item {
                        Text("Top Performing Categories", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (data.categoryDistribution.isNotEmpty()) {
                            val maxCount = data.categoryDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                            data.categoryDistribution.entries.sortedByDescending { it.value }.take(3).forEach { entry ->
                                val catName = entry.key
                                VendorPerformanceCard(catName, (entry.value * 100) / maxCount, "${entry.value} Orders")
                            }
                        } else {
                            Text("No category data available yet", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealLineChart(data: Map<String, Double>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val width = size.width
            val height = size.height
            val maxRevenue = data.values.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
            val minRevenue = 0f
            
            val entries = data.entries.toList()
            val points = entries.mapIndexed { index, entry ->
                val x = if (entries.size > 1) (index.toFloat() / (entries.size - 1)) * width else width / 2f
                val y = height - ((entry.value.toFloat() - minRevenue) / (maxRevenue - minRevenue) * height)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p1 = points[i - 1]
                        val p2 = points[i]
                        cubicTo(
                            (p1.x + p2.x) / 2, p1.y,
                            (p1.x + p2.x) / 2, p2.y,
                            p2.x, p2.y
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 4.dp.toPx())
                )
                
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            }
        }
    }
}



@Composable
private fun RealBarChart(hourlyDistribution: Map<Int, Int>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val hours = listOf(10, 12, 14, 16, 18, 20, 22)
            val labels = listOf("10A", "12P", "2P", "4P", "6P", "8P", "10P")
            val counts = hours.map { hourlyDistribution[it] ?: 0 }
            val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
            
            counts.zip(labels).forEach { (count, label) ->
                val hFraction = count.toFloat() / maxCount.toFloat()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(hFraction.coerceAtLeast(0.1f))
                            .background(Color(0xFFF16B24), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(label, color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(title: String, value: String, trend: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(trend, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun VendorPerformanceCard(name: String, score: Int, revenue: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = score / 100f,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFF2A2A2A)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(revenue, color = Color(0xFFF16B24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
