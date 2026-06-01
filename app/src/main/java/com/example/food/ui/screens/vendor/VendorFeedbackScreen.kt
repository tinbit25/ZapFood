package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import com.example.food.data.model.Feedback
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.FeedbackViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VendorFeedbackScreen(
    vendorId: String,
    onNavigateBack: () -> Unit,
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    val feedbackState by feedbackViewModel.vendorFeedbackState.collectAsState()

    LaunchedEffect(vendorId) {
        feedbackViewModel.fetchVendorFeedback(vendorId)
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(
                title = "Customer Reviews",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF0F0F0F))
        ) {
            when (val state = feedbackState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF16B24))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message ?: "Failed to load feedback",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { feedbackViewModel.fetchVendorFeedback(vendorId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24))
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    val feedbacks = state.data ?: emptyList()
                    if (feedbacks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No reviews yet.",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        val averageRating = feedbacks.map { it.rating }.average()
                        val totalReviews = feedbacks.size
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                RatingSummaryCard(
                                    averageRating = averageRating,
                                    totalReviews = totalReviews,
                                    feedbacks = feedbacks
                                )
                            }
                            
                            item {
                                Text(
                                    text = "Recent Feedback",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(feedbacks, key = { it.feedbackId }) { feedback ->
                                FeedbackItemRow(feedback = feedback)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingSummaryCard(
    averageRating: Double,
    totalReviews: Int,
    feedbacks: List<Feedback>
) {
    Surface(
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", averageRating),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val stars = averageRating.toInt()
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFF16B24),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalReviews reviews",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (stars in 5 downTo 1) {
                    val count = feedbacks.count { it.rating == stars }
                    val fraction = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "$stars ★", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(24.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp),
                            color = Color(0xFFF16B24),
                            trackColor = Color(0xFF2A2A2A),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = count.toString(),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.width(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackItemRow(feedback: Feedback) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feedback.userName.ifBlank { "Anonymous Customer" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                
                val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                val formattedDate = remember(feedback.createdAt) { sdf.format(Date(feedback.createdAt)) }
                Text(
                    text = formattedDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= feedback.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = Color(0xFFF16B24),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            if (feedback.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback.comment,
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
