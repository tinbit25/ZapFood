package com.example.food.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.data.model.MealPlan
import com.example.food.ui.components.MPCodeDialog

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    mealPlanViewModel: MealPlanViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToMealPlanDetails: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    
    val exploreCategories = listOf(
        ExploreItem("Restaurants", "🍟"),
        ExploreItem("Kravinz", "🥘"),
        ExploreItem("Chefs", "👩‍🍳"),
        ExploreItem("Cafes", "☕")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)), // Deep dark background
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            HomeHeader(
                userName = user?.displayName ?: "Kengfack Sydney",
                userPhotoUrl = user?.photoUrl,
                onNotificationClick = onNavigateToNotifications
            )
        }

        // Hottest Plans
        item {
            SectionTitle("Hottest Plans")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mealPlans.take(3)) { plan ->
                    HottestPlanCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.mealPlanId) })
                }
            }
        }

        // Explore
        item {
            SectionTitle("Explore")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                exploreCategories.forEach { item ->
                    ExploreCategoryItem(item)
                }
            }
        }

        // Plans made for you
        item {
            SectionTitle("Plans made for you")
        }
        
        items(mealPlans) { plan ->
            LargePlanCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.mealPlanId) })
        }
    }
}

data class ExploreItem(val name: String, val icon: String)

@Composable
fun HomeHeader(
    userName: String,
    userPhotoUrl: String?,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = userPhotoUrl ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop",
                contentDescription = "Profile",
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Deliver to", fontSize = 12.sp, color = Color.Gray)
                Text(text = userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Badge
            Surface(
                color = Color(0xFFF16B24),
                shape = CircleShape,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "1", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HottestPlanCard(plan: MealPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = plan.vendorName, fontSize = 10.sp, color = Color.LightGray)
                Text(text = plan.mealPlanName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🍳 ${plan.meals.size} meals", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ExploreCategoryItem(item: ExploreItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(65.dp),
            shape = CircleShape,
            color = Color(0xFF1A1A1A)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = item.icon, fontSize = 28.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = item.name, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
fun LargePlanCard(plan: MealPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(text = plan.vendorName, fontSize = 12.sp, color = Color.LightGray)
                Text(text = plan.mealPlanName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🍳 ${plan.meals.size} meals", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}
