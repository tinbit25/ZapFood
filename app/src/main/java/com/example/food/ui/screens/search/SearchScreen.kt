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
import com.example.food.data.model.Meal
import com.example.food.ui.screens.home.MealRow

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Placeholder items for search
    val allItems = listOf(
        Meal(
            mealId = "1",
            mealName = "Classic Cheeseburger",
            description = "Juicy beef patty with cheese",
            price = 8.99,
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop",
            calories = 600,
            proteins = 35f,
            carbs = 45f,
            fats = 30f,
            vendorName = "Burger King"
        ),
        Meal(
            mealId = "2",
            mealName = "Pepperoni Pizza",
            description = "Crispy crust with pepperoni",
            price = 12.99,
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop",
            calories = 800,
            proteins = 25f,
            carbs = 90f,
            fats = 35f,
            vendorName = "Pizza Hut"
        ),
        Meal(
            mealId = "3",
            mealName = "Spicy Tuna Roll",
            description = "Fresh tuna with spicy mayo",
            price = 10.50,
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop",
            calories = 450,
            proteins = 20f,
            carbs = 50f,
            fats = 15f,
            vendorName = "Sushi Zen"
        ),
        Meal(
            mealId = "4",
            mealName = "Chicken Salad",
            description = "Fresh greens with grilled chicken",
            price = 9.99,
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop",
            calories = 350,
            proteins = 40f,
            carbs = 15f,
            fats = 12f,
            vendorName = "Fresh Grill"
        )
    )

    val filteredItems = allItems.filter { it.mealName.contains(searchQuery, ignoreCase = true) }

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
                MealRow(meal = item, onClick = { onNavigateToDetails(item.mealId) })
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
