package com.example.food.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
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
import com.example.food.data.model.*
import com.example.food.domain.usecase.VendorAction
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AdminViewModel

@Composable
fun VendorManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val usersState by viewModel.usersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers(role = UserRole.VENDOR)
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopNavBar(title = "Vendor Approvals", onBackClick = onNavigateBack)

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Pending Applications",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (usersState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFF16B24))
                        }
                    }
                    is Resource.Error -> {
                        Text(text = usersState.message ?: "Error loading vendors", color = Color.Red)
                    }
                    is Resource.Success -> {
                        val vendors = usersState.data ?: emptyList()
                        if (vendors.isEmpty()) {
                            EmptyState("No vendor applications found")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(vendors) { vendor ->
                                    VendorApprovalCard(
                                        vendor = vendor,
                                        onAction = { action -> viewModel.updateVendorStatus(vendor.userId, action) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorApprovalCard(vendor: User, onAction: (VendorAction) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = Color(0xFFF16B24))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = vendor.displayName ?: "Unnamed Vendor", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = vendor.businessAddress, fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                StatusChip(vendor.vendorStatus)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (vendor.vendorStatus == VendorStatus.PENDING || vendor.vendorStatus == VendorStatus.REJECTED) {
                    Button(
                        onClick = { onAction(VendorAction.APPROVE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 12.sp)
                    }
                }
                
                if (vendor.vendorStatus == VendorStatus.PENDING) {
                    OutlinedButton(
                        onClick = { onAction(VendorAction.REJECT) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Reject", fontSize = 12.sp)
                    }
                }

                if (vendor.vendorStatus == VendorStatus.APPROVED) {
                    OutlinedButton(
                        onClick = { onAction(VendorAction.SUSPEND) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                    ) {
                        Text("Suspend", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: VendorStatus) {
    val color = when(status) {
        VendorStatus.PENDING -> Color(0xFFFF9800)
        VendorStatus.APPROVED -> Color(0xFF4CAF50)
        VendorStatus.REJECTED -> Color.Red
        VendorStatus.SUSPENDED -> Color.Gray
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = Color.Gray, fontSize = 14.sp)
    }
}
