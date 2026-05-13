package com.example.food.ui.screens.search

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
import coil.compose.AsyncImage
import com.example.food.data.model.Meal
import com.example.food.data.model.Vendor
import com.example.food.data.model.VendorType
import com.example.food.ui.viewmodel.SearchMode
import com.example.food.ui.viewmodel.SearchViewModel

@Composable
fun GlobalSearchScreen(
    onNavigateToMeal: (String) -> Unit,
    onNavigateToVendor: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            SearchHeader(
                query = query,
                onQueryChanged = viewModel::onQueryChanged,
                onBackClick = onNavigateBack,
                searchMode = uiState.searchMode,
                onModeChanged = viewModel::setMode
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorScheme.background)
        ) {
            // Ethiopian Quick Filters
            QuickFilterBar(
                isFasting = uiState.isFastingFilter,
                onFastingToggle = viewModel::toggleFastingFilter
            )

            if (query.isEmpty()) {
                InitialSearchContent(
                    recentSearches = uiState.recentSearches,
                    onRecentSearchClick = viewModel::onQueryChanged,
                    onClearHistory = viewModel::clearHistory
                )
            } else {
                SearchResultsContent(
                    mode = uiState.searchMode,
                    foodResults = uiState.foodResults,
                    vendorResults = uiState.vendorResults,
                    onMealClick = onNavigateToMeal,
                    onVendorClick = onNavigateToVendor,
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit,
    searchMode: SearchMode,
    onModeChanged: (SearchMode) -> Unit
) {
    Column(
        modifier = Modifier
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
            
            // Dual Mode Switcher
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                SearchModeTab(
                    title = "Foods",
                    isSelected = searchMode == SearchMode.FOOD,
                    onClick = { onModeChanged(SearchMode.FOOD) },
                    modifier = Modifier.weight(1f)
                )
                SearchModeTab(
                    title = "Vendors",
                    isSelected = searchMode == SearchMode.VENDOR,
                    onClick = { onModeChanged(SearchMode.VENDOR) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(if (searchMode == SearchMode.FOOD) "Search meals, snacks..." else "Search restaurants, cafes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
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
fun SearchModeTab(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
fun QuickFilterBar(isFasting: Boolean, onFastingToggle: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = isFasting,
                onClick = onFastingToggle,
                label = { Text("🌱 Fasting Foods") },
                leadingIcon = { if (isFasting) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) },
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text("🍖 Non-Fasting") },
                shape = RoundedCornerShape(12.dp)
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = {},
                label = { Text("🔥 Spicy") },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun InitialSearchContent(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Searches", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Clear", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onClearHistory() }
                    )
                }
            }
            items(recentSearches) { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecentSearchClick(search) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(search, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Text(
                "Trending Meals", 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val trending = listOf("Shiro", "Doro Wat", "Beyaynetu", "Kitfo", "Spris Juice")
                items(trending) { item ->
                    Surface(
                        modifier = Modifier.clickable { onRecentSearchClick(item) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(item, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultsContent(
    mode: SearchMode,
    foodResults: List<Meal>,
    vendorResults: List<Vendor>,
    onMealClick: (String) -> Unit,
    onVendorClick: (String) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        SearchShimmerContent(mode)
        return
    }

    if (mode == SearchMode.FOOD) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(foodResults) { meal ->
                FoodSearchResultCard(meal = meal, onClick = { onMealClick(meal.id) })
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(vendorResults) { vendor ->
                VendorSearchResultCard(vendor = vendor, onClick = { onVendorClick(vendor.userId) })
            }
        }
    }
}

@Composable
fun FoodSearchResultCard(meal: Meal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    meal.name, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    meal.businessName, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ETB ${meal.price.toInt()}", 
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Text(" ${String.format("%.1f", meal.averageRating)}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VendorSearchResultCard(vendor: Vendor, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Text(vendor.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    vendor.businessTypes.filterNotNull().joinToString(", ") { it.displayName },
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
}

@Composable
fun SearchShimmerContent(mode: SearchMode) {
    if (mode == SearchMode.FOOD) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                ) {}
            }
        }
    }
}

