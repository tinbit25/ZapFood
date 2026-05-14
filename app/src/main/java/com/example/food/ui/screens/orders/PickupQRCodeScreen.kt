package com.example.food.ui.screens.orders

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.ui.components.QRCodeDisplay
import com.example.food.ui.viewmodel.OrderTrackingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupQRCodeScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrderTrackingViewModel = viewModel()
) {
    val timelineResource by viewModel.timelineState.collectAsState()
    val context = LocalContext.current
    
    // Brightness Boost
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        
        activity?.window?.attributes = activity?.window?.attributes?.apply {
            screenBrightness = 1.0f
        }
        
        onDispose {
            activity?.window?.attributes = activity?.window?.attributes?.apply {
                screenBrightness = originalBrightness
            }
        }
    }

    LaunchedEffect(orderId) {
        viewModel.observeOrder(orderId)
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Pickup QR Code", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val resource = timelineResource) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFF16B24))
                }
                is Resource.Error -> {
                    Text(
                        text = resource.message ?: "Error loading QR",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val timeline = resource.data
                    if (timeline != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PickupInstructionCard()
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Surface(
                                modifier = Modifier.size(280.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White,
                                shadowElevation = 8.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    QRCodeDisplay(
                                        payload = timeline.pickupQRCode,
                                        size = 240.dp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            QRCountdownTimer(expiresAt = System.currentTimeMillis() + 300000) // Mock 5 min for now
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Token: ${timeline.pickupToken}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF16B24),
                                letterSpacing = 8.sp
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = { viewModel.observeOrder(orderId) },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh Code")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PickupInstructionCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF16B24))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Show this QR code to the vendor staff to verify your pickup.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun QRCountdownTimer(expiresAt: Long) {
    var timeLeft by remember { mutableStateOf(expiresAt - System.currentTimeMillis()) }
    
    LaunchedEffect(expiresAt) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft = expiresAt - System.currentTimeMillis()
        }
    }
    
    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Code expires in",
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            color = if (timeLeft < 60000) Color.Red else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
