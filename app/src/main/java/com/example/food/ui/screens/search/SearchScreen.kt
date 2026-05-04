package com.example.food.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.screens.home.FoodItem
import com.example.food.ui.screens.home.FoodItemRow

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Placeholder items for search
    val allItems = listOf(
        FoodItem("1", "Classic Cheeseburger", "Juicy beef patty with cheese", 8.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop"),
        FoodItem("2", "Pepperoni Pizza", "Crispy crust with pepperoni", 12.99, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop"),
        FoodItem("3", "Spicy Tuna Roll", "Fresh tuna with spicy mayo", 10.50, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop"),
        FoodItem("4", "Chicken Salad", "Fresh greens with grilled chicken", 9.99, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop")
    )

    val filteredItems = allItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            CustomTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredItems) { item ->
                FoodItemRow(item = item, onClick = { onNavigateToDetails(item.id) })
            }
            if (filteredItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
