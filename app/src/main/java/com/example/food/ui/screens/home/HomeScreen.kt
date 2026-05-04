package com.example.food.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    
    val categories = listOf(
        Pair("Burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop"),
        Pair("Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop"),
        Pair("Sushi", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop"),
        Pair("Dessert", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500&auto=format&fit=crop")
    )

    val popularItems = listOf(
        FoodItem("1", "Classic Cheeseburger", "Juicy beef patty with cheese", 8.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop"),
        FoodItem("2", "Pepperoni Pizza", "Crispy crust with pepperoni", 12.99, "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop"),
        FoodItem("3", "Spicy Tuna Roll", "Fresh tuna with spicy mayo", 10.50, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // padding for bottom bar
    ) {
        item {
            HeaderSection(
                userName = user?.displayName ?: "Guest",
                userPhotoUrl = user?.photoUrl,
                onSearchClick = onNavigateToSearch
            )
        }
        item {
            CategoriesSection(categories = categories)
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Popular Items",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(popularItems) { item ->
            FoodItemRow(item = item, onClick = { onNavigateToDetails(item.id) })
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    userPhotoUrl: String?,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hello, $userName", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "What would you like to eat?", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AsyncImage(
                model = userPhotoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500&auto=format&fit=crop",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.clickable { onSearchClick() }) {
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Search for food or restaurants",
                leadingIcon = Icons.Default.Search,
                enabled = false // Disable direct typing to trigger navigation
            )
        }
    }
}

@Composable
fun CategoriesSection(categories: List<Pair<String, String>>) {
    Column {
        Text(
            text = "Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = category.second,
                        contentDescription = category.first,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = category.first, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

data class FoodItem(val id: String, val name: String, val description: String, val price: Double, val imageUrl: String)

@Composable
fun FoodItemRow(item: FoodItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "$${item.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
