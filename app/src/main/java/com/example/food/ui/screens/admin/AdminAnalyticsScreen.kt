package com.example.food.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun AdminAnalyticsScreen() {
    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = { TopNavBar(title = "Platform Analytics") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("Revenue Trajectory", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MockLineChart()
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MiniStatCard("Avg Order Value", "ETB 450", "+12%", Modifier.weight(1f))
                    MiniStatCard("Customer LTV", "ETB 12K", "+5%", Modifier.weight(1f))
                }
            }

            item {
                Text("Peak Ordering Times", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                MockBarChart()
            }

            item {
                Text("Top Performing Vendors", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                VendorPerformanceCard("Ethiopian Skyline Hotel", 98, "ETB 1.2M")
                VendorPerformanceCard("Kategna Traditional", 85, "ETB 900K")
                VendorPerformanceCard("Kuriftu Resorts", 78, "ETB 750K")
            }
        }
    }
}

@Composable
private fun MockLineChart() {
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
            val points = listOf(
                Offset(0f, height * 0.8f),
                Offset(width * 0.2f, height * 0.6f),
                Offset(width * 0.4f, height * 0.7f),
                Offset(width * 0.6f, height * 0.4f),
                Offset(width * 0.8f, height * 0.3f),
                Offset(width, height * 0.1f)
            )

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
            
            // Draw gradient below line
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
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

@Composable
private fun MockBarChart() {
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
            val heights = listOf(0.3f, 0.5f, 0.8f, 1.0f, 0.7f, 0.4f, 0.9f)
            val labels = listOf("10A", "12P", "2P", "4P", "6P", "8P", "10P")
            
            heights.zip(labels).forEach { (h, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(h)
                            .background(Color(0xFFF16B24), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Spacer(Modifier.height(8.dp))
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
