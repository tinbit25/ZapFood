package com.example.food.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.data.model.VendorApplication
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.components.VendorDocumentViewer
import com.example.food.ui.components.VendorModerationPanel
import com.example.food.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@Composable
fun VendorVerificationDetailScreen(
    application: VendorApplication,
    adminViewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var activeDocTitle by remember { mutableStateOf<String?>(null) }
    var activeDocUrl by remember { mutableStateOf<String?>(null) }

    val vendor = application.vendor ?: return
    val user = application.user

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(
                title = vendor.businessName.ifBlank { "Vendor Verification" },
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Profile Summary Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vendor.businessName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(vendor.verificationStatus.name) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFF16B24).copy(alpha = 0.15f),
                                labelColor = Color(0xFFF16B24)
                            )
                        )
                    }
                }
            }

            // Geographic Details
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Geotag Coordinates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val lat = vendor.latitude ?: 9.03
                    val lng = vendor.longitude ?: 38.74
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(vendor.businessName)})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Google Maps is not installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map Location",
                            tint = Color(0xFFF16B24),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Latitude: $lat, Longitude: $lng",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Click to view location in Google Maps",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Licensing Documents Review Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verification Documents",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val info = vendor.verificationInfo
                    DocumentRowItem(
                        title = "Business License",
                        url = info?.businessLicenseUrl ?: "",
                        onClick = {
                            activeDocTitle = "Business License"
                            activeDocUrl = info?.businessLicenseUrl ?: ""
                        }
                    )
                    DocumentRowItem(
                        title = "Sanitation Certificate",
                        url = info?.sanitationCertificateUrl ?: "",
                        onClick = {
                            activeDocTitle = "Sanitation Certificate"
                            activeDocUrl = info?.sanitationCertificateUrl ?: ""
                        }
                    )
                    DocumentRowItem(
                        title = "National ID / Passport ID",
                        url = info?.nationalIdUrl ?: "",
                        onClick = {
                            activeDocTitle = "National ID / Passport"
                            activeDocUrl = info?.nationalIdUrl ?: ""
                        }
                    )
                }
            }

            // Payout Accounts Detail
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Financial & Payout Methods",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val info = vendor.verificationInfo
                    FinancialRowItem(
                        label = "Payout Account Holder Name",
                        value = info?.payoutAccountName ?: "N/A"
                    )
                    FinancialRowItem(
                        label = "Bank Account Information",
                        value = info?.bankAccountInfo ?: "N/A"
                    )
                    FinancialRowItem(
                        label = "Mobile Money Number",
                        value = info?.mobileMoneyNumber ?: "N/A"
                    )
                    FinancialRowItem(
                        label = "Tax ID / TIN Number",
                        value = info?.taxId ?: "N/A"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Panel
            VendorModerationPanel(
                onActionClick = { action ->
                    adminViewModel.updateVendorStatus(user.userId, action)
                    Toast.makeText(context, "Vendor action performed successfully!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            )
        }

        // Active BottomSheet Document Viewer overlay
        if (activeDocTitle != null && activeDocUrl != null) {
            VendorDocumentViewer(
                title = activeDocTitle!!,
                documentUrl = activeDocUrl!!,
                onDismiss = {
                    activeDocTitle = null
                    activeDocUrl = null
                }
            )
        }
    }
}

@Composable
fun DocumentRowItem(
    title: String,
    url: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Document Icon",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Preview",
            tint = Color(0xFFF16B24),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FinancialRowItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
