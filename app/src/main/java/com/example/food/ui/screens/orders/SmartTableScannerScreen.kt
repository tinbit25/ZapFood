package com.example.food.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
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
    
    // JSON Payload simulation
    val mockPayload = """{ "vendorId": "ETH_XYZ_01", "tableId": "T-05", "tableNumber": "5", "branchId": "DB_BRANCH_A" }"""

    LaunchedEffect(uiState.session) {
        if (uiState.session != null) {
            onSessionStarted()
        }
    }

    Scaffold(
        topBar = { TopNavBar(title = "Scan Table QR", onBackClick = onNavigateBack) },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color(0xFFF16B24)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Align the Table QR Code",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Scanning will automatically bind you to the table and show your running bill.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    user?.userId?.let { uid ->
                        viewModel.parseQRCode(mockPayload, uid)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simulate Scan (Table 5)", fontWeight = FontWeight.Bold)
            }
            
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
