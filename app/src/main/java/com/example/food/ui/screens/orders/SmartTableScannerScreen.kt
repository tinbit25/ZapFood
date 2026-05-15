package com.example.food.ui.screens.orders

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
import com.example.food.ui.viewmodel.SmartTableViewModel
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun SmartTableScannerScreen(
    onNavigateBack: () -> Unit,
    onSessionStarted: () -> Unit,
    viewModel: SmartTableViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user by userViewModel.user.collectAsState()
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
            user?.userId?.let { uid ->
                viewModel.parseQRCode(result.contents, uid)
            }
        }
    }

    LaunchedEffect(uiState.session) {
        if (uiState.session != null) {
            onSessionStarted()
        }
    }

    Scaffold(
        topBar = { TopNavBar(title = "Table Scanner", onBackClick = onNavigateBack) },
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
                        text = "Scan the QR code located on your table to automatically link your order and start your dining session.",
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
                // Scanner corner accents
                Box(modifier = Modifier.fillMaxSize()) {
                    // This is a simplified version of corner accents
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Arrived at the Hotel?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    scanLauncher.launch(
                        com.journeyapps.barcodescanner.ScanOptions().apply {
                            setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                            setPrompt("Scan Table QR Code")
                            setBeepEnabled(true)
                            setBarcodeImageEnabled(false)
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
                Text("Scan Table QR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
