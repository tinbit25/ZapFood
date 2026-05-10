package com.example.food.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.BorderStroke
import com.example.food.data.model.*
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
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.MealViewModel
import androidx.compose.material3.*
import com.example.food.core.util.Resource

@Composable
fun MealPlanDetailsScreen(
    planId: String,
    mealPlanViewModel: MealPlanViewModel,
    mealViewModel: MealViewModel,
    userViewModel: UserViewModel,
    cartViewModel: CartViewModel,
    rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedPlanState by mealPlanViewModel.selectedPlanState.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    val user by userViewModel.user.collectAsState()
    val generatedCodeResource by rewardViewModel.generatedCode.collectAsState()
    
    val allAvailableMeals = (mealsState as? Resource.Success)?.data ?: emptyList()
    var selectedDay by remember { mutableStateOf(Day.MONDAY) }
    var showCodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(planId) {
        mealPlanViewModel.fetchPlanById(planId)
    }

    if (showCodeDialog && generatedCodeResource is Resource.Success) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Share Plan", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share this code with friends! You'll earn 10 points when they import it.", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFF16B24))
                    ) {
                        Text(
                            text = (generatedCodeResource as Resource.Success<String>).data!!,
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFF16B24),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            letterSpacing = 4.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("Done", color = Color(0xFFF16B24))
                }
            }
        )
    }

    when (val state = selectedPlanState) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFF16B24))
            }
        }
        is Resource.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message ?: "Failed to load meal plan", color = Color.Red)
            }
        }
        is Resource.Success -> {
            val plan = state.data!!
            val isOwner = user?.userId == plan.ownerId

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F0F))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = plan.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = plan.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "ETB ${"%,.0f".format(plan.price * 1000)}/month",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(text = "by ${plan.vendorName}", fontSize = 14.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val totalMealsCount = plan.meals.values.sumOf { it.size }
                        StatItem("🍳", "$totalMealsCount meals")
                        StatItem("🍱", "${plan.nutritionalSummary.totalCalories} kcal")
                        StatItem("📑", if (plan.mpcode.isNotEmpty()) plan.mpcode else "No Code")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Day Selector
                    ScrollableTabRow(
                        selectedTabIndex = Day.entries.indexOf(selectedDay),
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFF16B24),
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = {}
                    ) {
                        Day.entries.forEach { day ->
                            val isSelected = selectedDay == day
                            Tab(
                                selected = isSelected,
                                onClick = { selectedDay = day },
                                text = {
                                    Surface(
                                        color = if (isSelected) Color(0xFFF16B24) else Color(0xFF1A1A1A),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = day.name.lowercase().replaceFirstChar { it.uppercase() }.take(3),
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            color = if (isSelected) Color.White else Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Meal List for Selected Day
                    val mealIds = plan.meals[selectedDay] ?: emptyList()
                    if (mealIds.isEmpty()) {
                        Text(text = "No meals planned for this day", color = Color.Gray, modifier = Modifier.padding(32.dp))
                    } else {
                        mealIds.forEach { mealId ->
                            val meal = allAvailableMeals.find { it.id == mealId }
                            if (meal != null) {
                                MealDetailItem(meal)
                                Divider(color = Color(0xFF1A1A1A), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PrimaryButton(
                            text = "Add to Cart",
                            onClick = { 
                                cartViewModel.addMealPlan(plan)
                                onNavigateBack()
                            },
                            backgroundColor = Color(0xFFF16B24)
                        )
                    }
                    
                    if (isOwner) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { 
                                    user?.let {
                                        rewardViewModel.generateCode(it, plan.id)
                                        showCodeDialog = true
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1A1A1A),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (generatedCodeResource is Resource.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFF16B24), strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
fun MealDetailItem(meal: Meal) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = meal.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = meal.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "${meal.calories} kcal | P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fats}g", fontSize = 11.sp, color = Color(0xFFF16B24))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meal.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp,
                maxLines = 2
            )
        }
    }
}
