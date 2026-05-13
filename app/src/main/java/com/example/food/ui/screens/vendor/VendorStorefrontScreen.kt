package com.example.food.ui.screens.vendor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor
import com.example.food.ui.viewmodel.VendorStorefrontViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorStorefrontScreen(
    vendorId: String,
    onNavigateBack: () -> Unit,
    onMealClick: (String) -> Unit,
    viewModel: VendorStorefrontViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(vendorId) {
        viewModel.loadVendorStorefront(vendorId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.vendor?.businessName ?: "Vendor Store", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            ShimmerStorefrontContent(padding)
        } else if (uiState.vendor != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // A. Vendor Header
                item {
                    VendorHeaderSection(vendor = uiState.vendor!!)
                }

                // Vendor Info Card
                item {
                    VendorInfoCard(vendor = uiState.vendor!!)
                }

                // C. Internal Vendor Search
                item {
                    VendorMealSearchBar(
                        businessName = uiState.vendor!!.businessName,
                        query = uiState.searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged
                    )
                }

                // B. Menu Organization (Sticky Category Tabs)
                item {
                    VendorMenuTabs(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected
                    )
                }

                // D. Menu Display (Meal Grid/List)
                if (uiState.filteredMeals.isEmpty()) {
                    item {
                        EmptyMenuPlaceholder()
                    }
                } else {
                    items(uiState.filteredMeals.chunked(2)) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { meal ->
                                Box(modifier = Modifier.weight(1f)) {
                                    MealCard(meal = meal, onClick = { onMealClick(meal.id) })
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorHeaderSection(vendor: Vendor) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        AsyncImage(
            model = vendor.coverImageUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Ethiopian Flag Accent Overlay (Top)
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
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                    )
                )
        )

        // Logo
        AsyncImage(
            model = vendor.logoUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=200",
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.Crop
        )

        // Status Badge
        val isOpen = true // Mock
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                if (isOpen) "OPEN NOW" else "CLOSED",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun VendorInfoCard(vendor: Vendor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = vendor.businessName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = vendor.businessTypes.joinToString(" • ") { it.displayName },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoChip(icon = Icons.Default.Star, label = "${vendor.rating} (500+)", color = Color(0xFFFFB300))
            InfoChip(icon = Icons.Default.Timer, label = "${vendor.deliveryTimeMin}-${vendor.deliveryTimeMax} min")
            InfoChip(icon = Icons.Default.LocalShipping, label = if (vendor.deliveryFee == 0.0) "Free" else "$${vendor.deliveryFee}")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = vendor.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun VendorMealSearchBar(businessName: String, query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search inside $businessName...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun VendorMenuTabs(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = if (!isSelected) FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = MaterialTheme.colorScheme.outline
                ) else null
            )
        }
    }
}

@Composable
fun MealCard(meal: Meal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp)) {
                AsyncImage(
                    model = meal.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Badge System
                MealBadgeSystem(meal)
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${meal.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("15 min", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MealBadgeSystem(meal: Meal) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Fasting Badge
        if (meal.fastingFriendly) {
            Badge(containerColor = Color(0xFF4CAF50), contentColor = Color.White) {
                Text("FASTING", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bestseller Badge (Simulated)
        if (meal.popularityScore > 0.8) {
            Badge(containerColor = Color(0xFFFF9800), contentColor = Color.White) {
                Text("BESTSELLER", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Spice Level
        if (meal.spiceLevel.name != "NONE") {
            Badge(containerColor = Color(0xFFE91E63), contentColor = Color.White) {
                Text(
                    text = when(meal.spiceLevel.name) {
                        "MILD" -> "🌶️"
                        "MEDIUM" -> "🌶️🌶️"
                        "SPICY" -> "🌶️🌶️🌶️"
                        "VERY_SPICY" -> "🔥"
                        else -> ""
                    },
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun EmptyMenuPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No items found in this category", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ShimmerStorefrontContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Hero Shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray.copy(alpha = 0.3f))
        )

        // Info Shimmer
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(width = 200.dp, height = 32.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(width = 150.dp, height = 16.dp).background(Color.LightGray.copy(alpha = 0.3f)))
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(3) {
                    Box(modifier = Modifier.size(width = 80.dp, height = 24.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray.copy(alpha = 0.3f)))
                }
            }
        }

        // Search Shimmer
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        )

        // Category Tabs Shimmer
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                Box(modifier = Modifier.size(width = 90.dp, height = 32.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray.copy(alpha = 0.3f)))
            }
        }

        // Items Shimmer
        Spacer(Modifier.height(16.dp))
        repeat(2) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

