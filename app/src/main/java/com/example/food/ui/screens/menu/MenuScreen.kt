package com.example.food.ui.screens.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.screens.home.CategoriesSection

@Composable
fun MenuScreen() {
    val categories = listOf(
        Pair("Burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop"),
        Pair("Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop"),
        Pair("Sushi", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop"),
        Pair("Dessert", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500&auto=format&fit=crop"),
        Pair("Drinks", "https://images.unsplash.com/photo-1544145945-f90425340c7e?w=500&auto=format&fit=crop"),
        Pair("Salad", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500&auto=format&fit=crop")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "Full Menu")
        
        Column(modifier = Modifier.padding(top = 16.dp)) {
            CategoriesSection(categories = categories)
        }
        
        // In a real app, clicking a category would filter a list below.
        // For now, we'll just show the categories.
    }
}
