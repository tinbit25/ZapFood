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
import com.example.food.data.model.Meal
import com.example.food.data.model.MealPlan
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
import androidx.compose.material3.*

@Composable
fun MealPlanDetailsScreen(
    planId: String,
    mealPlanViewModel: MealPlanViewModel,
    userViewModel: UserViewModel,
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit
) {
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    val plan = mealPlans.find { it.id == planId } ?: return

    var selectedTab by remember { mutableStateOf("Lunch") }
    val tabs = listOf("Overview", "Breakfast", "Lunch", "Dinner", "Extras")

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
            
            // Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Vendor Logo Overlay
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 50.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(4.dp, Color(0xFF0F0F0F))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = "https://img.freepik.com/free-vector/chef-logo-template-design_23-2150702462.jpg", // Mock logo
                        contentDescription = "Vendor Logo",
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = plan.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "RWF ${"%,.0f".format(plan.price * 1000)}/month", // Assuming price is in 'k' units or adjusting for RWF
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
                StatItem("🍳", "${plan.meals.size} meals")
                StatItem("🍱", "${plan.nutritionalSummary.totalCalories} kcal")
                StatItem("🛒", "2.5k Orders")
                StatItem("📑", "MP Code")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color.Transparent,
                contentColor = Color(0xFFF16B24),
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Surface(
                                color = if (isSelected) Color(0xFFF16B24) else Color(0xFF1A1A1A),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = tab,
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

            // Meal List
            plan.meals.forEach { meal ->
                MealDetailItem(meal)
                Divider(color = Color(0xFF1A1A1A), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
            }
        }

        // Add to Cart and Share Row
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
            
            // Share Button
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { 
                        // Simulate sharing and reward points
                        userViewModel.updateRewardPoints(10)
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A1A1A),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}
