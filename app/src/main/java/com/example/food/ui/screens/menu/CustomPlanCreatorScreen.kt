package com.example.food.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.data.model.Meal
import java.util.UUID

@Composable
fun CustomPlanCreatorScreen(
    mealViewModel: com.example.food.ui.viewmodel.MealViewModel,
    mealPlanViewModel: com.example.food.ui.viewmodel.MealPlanViewModel,
    onNavigateBack: () -> Unit,
    onPlanCreated: () -> Unit
) {
    val mealsState by mealViewModel.mealsState.collectAsState()
    val availableMeals = (mealsState as? com.example.food.core.util.Resource.Success)?.data ?: emptyList()
    
    var selectedMeals by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Custom Plan", onBackClick = onNavigateBack)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Select meals for your plan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(availableMeals) { meal ->
                    val isSelected = selectedMeals.contains(meal.id)
                    CustomMealItem(
                        meal = meal,
                        isSelected = isSelected,
                        onClick = {
                            selectedMeals = if (isSelected) {
                                selectedMeals - meal.id
                            } else {
                                selectedMeals + meal.id
                            }
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.padding(24.dp)) {
            PrimaryButton(
                text = "Create Plan (${selectedMeals.size} meals)",
                onClick = onPlanCreated,
                enabled = selectedMeals.isNotEmpty(),
                backgroundColor = Color(0xFFF16B24)
            )
        }
    }
}

@Composable
fun CustomMealItem(
    meal: Meal,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected) Color(0xFFF16B24).copy(alpha = 0.1f) else Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF16B24)) else null
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${meal.calories} kcal", fontSize = 12.sp, color = Color.Gray)
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFF16B24),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
