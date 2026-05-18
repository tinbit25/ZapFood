package com.example.food.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import coil.compose.AsyncImage
import com.example.food.ui.components.CustomTextField
import com.example.food.data.model.Meal
import java.util.UUID

@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val vendorId = java.util.UUID.randomUUID().toString()
    
    // Placeholder items for search
    val allItems = listOf(
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Classic Cheeseburger",
            description = "Juicy beef patty with cheese",
            price = 8.99,
            imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop",
            vendorId = vendorId,
            businessName = "Burger King"
        ),
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Pepperoni Pizza",
            description = "Crispy crust with pepperoni",
            price = 12.99,
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop",
            vendorId = vendorId,
            businessName = "Pizza Hut"
        ),
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Spicy Tuna Roll",
            description = "Fresh tuna with spicy mayo",
            price = 10.50,
            imageUrl = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop",
            vendorId = vendorId,
            businessName = "Sushi Zen"
        ),
        Meal(
            id = java.util.UUID.randomUUID().toString(),
            name = "Chicken Salad",
            description = "Fresh greens with grilled chicken",
            price = 9.99,
            imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop",
            vendorId = vendorId,
            businessName = "Fresh Grill"
        )
    )

    val filteredItems = allItems.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            CustomTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search meals...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredItems) { item ->
                MealSearchItem(meal = item, onClick = { onNavigateToDetails(item.id) })
            }
            if (filteredItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No results found", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun MealSearchItem(meal: Meal, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = meal.businessName, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "ETB ${"%,.0f".format(meal.price)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
