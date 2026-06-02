package com.example.food.ui.screens.vendor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.VendorMenuQRViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorMenuQRScreen(
    vendorId: String,
    onNavigateBack: () -> Unit,
    viewModel: VendorMenuQRViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(vendorId) {
        viewModel.loadVendorAndGenerateQR(vendorId)
    }

    Scaffold(
        topBar = { TopNavBar(title = "Menu QR Code", onBackClick = onNavigateBack) },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF16B24)
                    )
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Error",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.qrBitmap != null -> {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Menu QR Code",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Customers can scan this QR code to view your menu and place orders directly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // QR Code Display
                    Surface(
                        modifier = Modifier.size(300.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = uiState.qrBitmap!!.asImageBitmap(),
                                contentDescription = "Menu QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Vendor Name
                    uiState.vendor?.let { vendor ->
                        Text(
                            text = vendor.businessName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan to view menu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Regenerate Button
                    Button(
                        onClick = { viewModel.regenerateQR() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate QR", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructions
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "How to use:",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Print this QR code and place it at your entrance\n• Customers scan it to view your menu\n• They can place orders directly from their phone",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
