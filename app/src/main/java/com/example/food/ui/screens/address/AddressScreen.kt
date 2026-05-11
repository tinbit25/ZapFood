package com.example.food.ui.screens.address

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.data.model.Address
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AddressViewModel
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun AddressScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToEditAddress: (String) -> Unit,
    userViewModel: UserViewModel,
    addressViewModel: AddressViewModel
) {
    val user by userViewModel.user.collectAsState()
    val addresses by addressViewModel.addresses.collectAsState()
    val isLoading by addressViewModel.isLoading.collectAsState()

    LaunchedEffect(user) {
        user?.userId?.let { addressViewModel.fetchAddresses(it) }
    }

    Scaffold(
        topBar = { TopNavBar(title = "My Addresses", onBackClick = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAddress,
                containerColor = Color(0xFFF16B24),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address")
            }
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading && addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF16B24))
                }
            } else if (addresses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No addresses saved yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(addresses) { address ->
                        AddressItem(
                            address = address,
                            onSetDefault = { user?.userId?.let { addressViewModel.setDefaultAddress(it, address.addressId) } },
                            onDelete = { user?.userId?.let { addressViewModel.deleteAddress(it, address.addressId) } },
                            onEdit = { onNavigateToEditAddress(address.addressId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    address: Address,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFF16B24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = address.label,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                if (address.isDefault) {
                    Surface(
                        color = Color(0xFFF16B24).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DEFAULT",
                            color = Color(0xFFF16B24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${address.city}, ${address.subcity}, Woreda ${address.woreda}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Text(
                text = "Kebele ${address.kebele}, ${address.street}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            if (address.landmark.isNotEmpty()) {
                Text(
                    text = "Landmark: ${address.landmark}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!address.isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Text("Set Default", color = Color(0xFFF16B24))
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }
        }
    }
}
