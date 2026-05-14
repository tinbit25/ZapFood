package com.example.food.ui.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.QRCodeDisplay
import com.example.food.ui.viewmodel.OrderTrackingViewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.PaymentStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRPaymentScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: OrderTrackingViewModel = viewModel()
) {
    val timelineResource by viewModel.timelineState.collectAsState()
    var selectedMethod by remember { mutableStateOf("Telebirr") }
    var isSimulatingPayment by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.observeOrder(orderId)
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopAppBar(
                title = { Text("Scan to Pay", color = Color.White) },
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
                is Resource.Success -> {
                    val order = resource.data
                    if (order != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Amount",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ETB ${"%,.2f".format(150.0)}", // Mock total
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            PaymentMethodSelector(
                                selected = selectedMethod,
                                onSelect = { selectedMethod = it }
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Surface(
                                modifier = Modifier.size(240.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    QRCodeDisplay(
                                        payload = "PAYMENT-$selectedMethod-$orderId-150.0",
                                        size = 200.dp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Scan this QR with your $selectedMethod app",
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = { 
                                    isSimulatingPayment = true
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isSimulatingPayment
                            ) {
                                if (isSimulatingPayment) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("I have paid", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
    
    if (isSimulatingPayment) {
        LaunchedEffect(Unit) {
            delay(3000)
            onPaymentSuccess()
        }
    }
}

@Composable
fun PaymentMethodSelector(selected: String, onSelect: (String) -> Unit) {
    val methods = listOf("Telebirr", "CBE Birr", "Chapa")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        methods.forEach { method ->
            val isSelected = selected == method
            Surface(
                onClick = { onSelect(method) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFFF16B24).copy(alpha = 0.1f) else Color(0xFF1A1A1A),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF16B24)) else null
            ) {
                Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = method,
                        color = if (isSelected) Color(0xFFF16B24) else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
