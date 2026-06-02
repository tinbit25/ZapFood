package com.example.food.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.VendorMenuQRScannerViewModel

@Composable
fun VendorMenuQRScannerScreen(
    onNavigateBack: () -> Unit,
    onVendorFound: (String) -> Unit,
    viewModel: VendorMenuQRScannerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Brightness Boost for better camera focus/visibility
    DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val originalBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = 1.0f }
        onDispose { activity?.window?.attributes = activity?.window?.attributes?.apply { screenBrightness = originalBrightness } }
    }

    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        com.journeyapps.barcodescanner.ScanContract()
    ) { result ->
        if (result.contents != null) {
            viewModel.parseQRCode(result.contents)
        }
    }

    LaunchedEffect(uiState.scanSuccess) {
        if (uiState.scanSuccess && uiState.vendor != null) {
            onVendorFound(uiState.vendor!!.id)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { TopNavBar(title = "Scan Vendor QR", onBackClick = onNavigateBack) },
        containerColor = Color(0xFF0F0F0F)
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
                        text = "Scan the vendor's QR code to view their menu and place orders directly.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFF16B24)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Scan Vendor QR Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Found a QR code at a restaurant?",
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    scanLauncher.launch(
                        com.journeyapps.barcodescanner.ScanOptions().apply {
                            setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                            setPrompt("Scan Vendor QR Code")
                            setBeepEnabled(true)
                            setBarcodeImageEnabled(false)
                            setOrientationLocked(false)
                            setCameraId(0) // 0 = back camera
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Scan QR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = uiState.error!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
