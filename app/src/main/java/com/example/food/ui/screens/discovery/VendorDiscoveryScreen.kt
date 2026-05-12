package com.example.food.ui.screens.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.core.util.Resource
import com.example.food.data.model.ServiceTag
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.VendorDiscoveryViewModel

@Composable
fun VendorDiscoveryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVendor: (String) -> Unit,
    viewModel: VendorDiscoveryViewModel = viewModel()
) {
    val vendorsState by viewModel.vendorsState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "Discover Vendors", onBackClick = onNavigateBack)

        // Search and Filters
        Column(modifier = Modifier.padding(16.dp)) {
            CustomTextField(
                value = searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = "Search business name...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Business Types
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { viewModel.filterByType(null) },
                        label = { Text("All Types") }
                    )
                }
                items(VendorType.entries) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { viewModel.filterByType(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Service Tags
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedTag == null,
                        onClick = { viewModel.filterByTag(null) },
                        label = { Text("Any Service") }
                    )
                }
                items(ServiceTag.entries) { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { viewModel.filterByTag(tag) },
                        label = { Text("${tag.icon} ${tag.displayName}") }
                    )
                }
            }
        }

        // Vendor List
        Box(modifier = Modifier.weight(1f)) {
            when (val state = vendorsState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(state.message ?: "Unknown error", modifier = Modifier.align(Alignment.Center), color = colorScheme.error)
                }
                is Resource.Success -> {
                    val vendors = state.data ?: emptyList()
                    if (vendors.isEmpty()) {
                        Text("No vendors found", modifier = Modifier.align(Alignment.Center), color = colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(vendors) { vendor ->
                                VendorCard(vendor = vendor, onClick = { onNavigateToVendor(vendor.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorCard(vendor: Vendor, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth()) {
                AsyncImage(
                    model = vendor.logoUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Floating Rating
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(vendor.rating.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Service Tags overlay
                Row(
                    modifier = Modifier.padding(12.dp).align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    vendor.serviceTags.take(3).forEach { tag ->
                        Surface(
                            color = colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tag.icon,
                                modifier = Modifier.padding(4.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(vendor.businessName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (vendor.isVerified) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
                
                // Multi-Type display
                Text(
                    text = vendor.businessTypes.joinToString(" • ") { it.displayName },
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = vendor.description,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 16.sp
                )

                Spacer(Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("${vendor.deliveryRadiusKm} km radius", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
