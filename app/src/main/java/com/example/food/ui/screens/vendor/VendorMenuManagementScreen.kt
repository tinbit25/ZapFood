package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.MealViewModel
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun VendorMenuManagementScreen(
    userViewModel: UserViewModel,
    mealViewModel: MealViewModel,
    onNavigateBack: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    
    LaunchedEffect(user) {
        user?.let {
            mealViewModel.updateFilters(MealFilters(vendorId = it.userId))
        }
    }

    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(
                title = "Menu Management",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Implement Add Meal logic */ },
                containerColor = Color(0xFFF16B24),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val state = mealsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF16B24))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "Error loading meals", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val meals = state.data ?: emptyList()
                    if (meals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "You haven't added any meals yet", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(meals) { meal ->
                                VendorMealItem(meal = meal)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VendorMealItem(meal: Meal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(text = meal.category, fontSize = 12.sp, color = Color(0xFFF16B24))
                Text(text = "${meal.calories} kcal", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = "RWF ${"%,.0f".format(meal.price * 1000)}",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
