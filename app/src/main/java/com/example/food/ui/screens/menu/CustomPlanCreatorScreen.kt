package com.example.food.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.data.model.Meal
import com.example.food.ui.screens.home.MealRow

@Composable
fun CustomPlanCreatorScreen(
    mealPlanViewModel: MealPlanViewModel,
    onNavigateBack: () -> Unit,
    onPlanCreated: () -> Unit
) {
    // In a real app, we'd fetch all individual meals from a repository
    // For now, we'll extract them from existing meal plans in the VM
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    val availableMeals = mealPlans.flatMap { it.meals }.distinctBy { it.mealId }
    
    var selectedMeals by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "Create Custom Plan", onBackClick = onNavigateBack)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Select meals for your plan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(availableMeals) { meal ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        MealRow(meal = meal, onClick = {
                            selectedMeals = if (selectedMeals.contains(meal.mealId)) {
                                selectedMeals - meal.mealId
                            } else {
                                selectedMeals + meal.mealId
                            }
                        })
                        
                        if (selectedMeals.contains(meal.mealId)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 32.dp, top = 16.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.padding(24.dp)) {
            PrimaryButton(
                text = "Create Plan (${selectedMeals.size} meals)",
                onClick = onPlanCreated,
                enabled = selectedMeals.isNotEmpty()
            )
        }
    }
}
