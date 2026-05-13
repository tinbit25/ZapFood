package com.example.food.ui.screens.vendor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.ui.viewmodel.VendorDiscoveryViewModel

@Composable
fun VendorDiscoveryScreen(
    onNavigateToVendor: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: VendorDiscoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            DiscoveryTopBar(
                searchQuery = searchQuery,
                onSearchChanged = viewModel::onSearchQueryChanged,
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            ShimmerDiscoveryContent(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Category Filter
                item {
                VendorCategoryFilter(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::onCategorySelected
                )
                }

                if (searchQuery.isNotBlank() || selectedCategory != null) {
                    // Search Results
                    item {
                        SectionHeader(title = "Results", showViewAll = false)
                    }
                    items(uiState.filteredVendors) { vendor ->
                        VendorListCard(vendor = vendor, onClick = { onNavigateToVendor(vendor.userId) })
                    }
                } else {
                    // Main Discovery Sections
                    item {
                        VendorSectionRow(
                            title = "Top Rated",
                            vendors = uiState.topRated,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "Popular Near You",
                            vendors = uiState.popular,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "Traditional Ethiopian",
                            vendors = uiState.traditional,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "Fast Delivery",
                            vendors = uiState.fastDelivery,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "Cafes & Coffee",
                            vendors = uiState.cafes,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "New on ZapFood",
                            vendors = uiState.newVendors,
                            onVendorClick = onNavigateToVendor
                        )
                    }

                    item {
                        VendorSectionRow(
                            title = "Budget Friendly",
                            vendors = uiState.budget,
                            onVendorClick = onNavigateToVendor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryTopBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Explore Vendors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search restaurants or cuisines...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )
    }
}

@Composable
fun VendorCategoryFilter(
    selectedCategory: VendorType?,
    onCategorySelected: (VendorType?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                shape = RoundedCornerShape(12.dp)
            )
        }
        items(VendorType.entries) { type ->
            FilterChip(
                selected = selectedCategory == type,
                onClick = { onCategorySelected(type) },
                label = { Text(type.displayName) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun VendorSectionRow(
    title: String,
    vendors: List<Vendor>,
    onVendorClick: (String) -> Unit
) {
    if (vendors.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionHeader(title = title, onViewAllClick = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(vendors) { vendor ->
                VendorCard(vendor = vendor, onClick = { onVendorClick(vendor.userId) })
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, showViewAll: Boolean = true, onViewAllClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (showViewAll) {
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }
    }
}

@Composable
fun VendorCard(vendor: Vendor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                AsyncImage(
                    model = vendor.coverImageUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Ethiopian Flag Accent Overlay (Subtle)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.TopStart)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF009B4D), Color(0xFFFED100), Color(0xFFEF3340))
                            )
                        )
                )

                // Logo Overlay
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(48.dp)
                        .align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    AsyncImage(
                        model = vendor.logoUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=100",
                        contentDescription = null,
                        modifier = Modifier.padding(2.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // Status Badge
                val isOpen = true // To be replaced with real check
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                ) {
                    Text(
                        if (isOpen) "OPEN" else "CLOSED",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delivery Time Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${vendor.deliveryTimeMin}-${vendor.deliveryTimeMax} min",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vendor.businessName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                String.format("%.1f", vendor.rating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vendor.businessTypes.joinToString(" • ") { it.displayName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Distance (Simulated)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                        Text(
                            "1.2 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping, 
                        contentDescription = null, 
                        tint = Color(0xFF009B4D), // Ethiopian Green
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (vendor.deliveryFee == 0.0) "Free Delivery" else "$${vendor.deliveryFee} Delivery",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (vendor.deliveryFee == 0.0) Color(0xFF009B4D) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerDiscoveryContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Shimmer Categories
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(5) {
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }

        // Shimmer Sections
        repeat(3) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .size(width = 120.dp, height = 24.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .size(width = 280.dp, height = 240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VendorListCard(vendor: Vendor, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = vendor.logoUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=200",
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                vendor.businessName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                vendor.businessTypes.joinToString(", ") { it.displayName },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                Text(" ${vendor.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(" • ${vendor.deliveryTimeMin} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}
