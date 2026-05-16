package com.example.food.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private enum class ScanResult { IDLE, SCANNING, SUCCESS, FAILURE }

@Composable
fun VendorPickupScannerScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    var scanState by remember { mutableStateOf(ScanResult.IDLE) }
    var scannedOrderId by remember { mutableStateOf("") }
    var scannedToken by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // Expected QR payload format: "PICKUP:<orderId>:<token>"
            val parts = result.contents.split(":")
            if (parts.size >= 3 && parts[0] == "PICKUP") {
                scannedOrderId = parts[1]
                scannedToken = parts[2]
                scanState = ScanResult.SCANNING
                // Verify the pickup token against the backend
                orderViewModel.verifyPickup(scannedOrderId, scannedToken) { res ->
                    when (res) {
                        is Resource.Success -> {
                            message = "✓ Pickup verified! Order #${scannedOrderId.take(6).uppercase()} handed off."
                            scanState = ScanResult.SUCCESS
                        }
                        is Resource.Error -> {
                            message = res.message ?: "Invalid or expired QR code."
                            scanState = ScanResult.FAILURE
                        }
                        else -> {}
                    }
                }
            } else {
                message = "Invalid QR code. Ask customer to refresh their pickup code."
                scanState = ScanResult.FAILURE
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = { TopNavBar(title = "Scan Pickup QR", onBackClick = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Instruction Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF16B24))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ask the customer to open their order tracking screen and show their QR code. Scan it to confirm pickup.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Central icon / status display
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        when (scanState) {
                            ScanResult.SUCCESS -> Brush.radialGradient(
                                listOf(Color(0xFF4CAF50).copy(alpha = 0.3f), Color(0xFF0F0F0F))
                            )
                            ScanResult.FAILURE -> Brush.radialGradient(
                                listOf(Color.Red.copy(alpha = 0.3f), Color(0xFF0F0F0F))
                            )
                            else -> Brush.radialGradient(
                                listOf(Color(0xFFF16B24).copy(alpha = 0.15f), Color(0xFF0F0F0F))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (scanState) {
                    ScanResult.SCANNING -> CircularProgressIndicator(color = Color(0xFFF16B24), strokeWidth = 3.dp)
                    ScanResult.SUCCESS -> Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    ScanResult.FAILURE -> Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Red
                    )
                    ScanResult.IDLE -> Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFFF16B24)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic title
            Text(
                text = when (scanState) {
                    ScanResult.IDLE -> "Ready to Scan"
                    ScanResult.SCANNING -> "Verifying…"
                    ScanResult.SUCCESS -> "Order Handed Off!"
                    ScanResult.FAILURE -> "Verification Failed"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            // Result message
            AnimatedVisibility(visible = message.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (scanState == ScanResult.SUCCESS) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (scanState == ScanResult.SUCCESS) Color(0xFF4CAF50).copy(alpha = 0.4f)
                        else Color.Red.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (scanState == ScanResult.SUCCESS) Color(0xFF4CAF50) else Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Main action button
            Button(
                onClick = {
                    scanState = ScanResult.IDLE
                    message = ""
                    scanLauncher.launch(
                        ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan Customer Pickup QR Code")
                            setBeepEnabled(true)
                            setBarcodeImageEnabled(false)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (scanState == ScanResult.IDLE) "Scan Customer QR" else "Scan Another",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual token entry fallback
            var showManualEntry by remember { mutableStateOf(false) }
            TextButton(onClick = { showManualEntry = !showManualEntry }) {
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enter Token Manually", color = Color.Gray, fontSize = 13.sp)
            }

            AnimatedVisibility(visible = showManualEntry) {
                ManualTokenEntry(
                    onVerify = { ordId, tok ->
                        scannedOrderId = ordId
                        scannedToken = tok
                        scanState = ScanResult.SCANNING
                        orderViewModel.verifyPickup(ordId, tok) { res ->
                            when (res) {
                                is Resource.Success -> {
                                    message = "✓ Pickup verified! Order #${ordId.take(6).uppercase()} marked as delivered."
                                    scanState = ScanResult.SUCCESS
                                }
                                is Resource.Error -> {
                                    message = res.message ?: "Invalid token."
                                    scanState = ScanResult.FAILURE
                                }
                                else -> {}
                            }
                        }
                        showManualEntry = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ManualTokenEntry(onVerify: (orderId: String, token: String) -> Unit) {
    var orderId by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = orderId,
            onValueChange = { orderId = it },
            label = { Text("Order ID", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF16B24),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFF16B24)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = token,
            onValueChange = { token = it.uppercase() },
            label = { Text("6-Digit Token", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF16B24),
                unfocusedBorderColor = Color(0xFF2A2A2A),
                focusedTextColor = Color(0xFFF16B24),
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFF16B24)
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Button(
            onClick = { if (orderId.isNotBlank() && token.isNotBlank()) onVerify(orderId.trim(), token.trim()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF16B24).copy(alpha = 0.5f))
        ) {
            Text("Verify Token", color = Color(0xFFF16B24), fontWeight = FontWeight.Bold)
        }
    }
}
