package com.example.food.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
    val discoverPlansState by mealPlanViewModel.discoverPlansState.collectAsState()
    val mealPlans = (discoverPlansState as? com.example.food.core.util.Resource.Success)?.data ?: emptyList()
    
    val exploreCategories = listOf(
        ExploreItem("Restaurants", "🍟"),
        ExploreItem("Kravinz", "🥘"),
        ExploreItem("Chefs", "👩‍🍳"),
        ExploreItem("Cafes", "☕")
    )

    LaunchedEffect(Unit) {
        mealPlanViewModel.fetchDiscoverPlans()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)), // Deep dark background
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            HomeHeader(
                userName = user?.displayName ?: "User Name",
                userPhotoUrl = user?.photoUrl,
                onNotificationClick = onNavigateToNotifications
            )
        }

        // Search Bar
        item {
            HomeSearchBar(onSearchClick = onNavigateToSearch)
        }

        // Hottest Plans
        item {
            SectionTitle("Hottest Plans")
            if (discoverPlansState is com.example.food.core.util.Resource.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = Color(0xFFF16B24))
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mealPlans.take(3)) { plan ->
                    HottestPlanCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
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
            LargePlanCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
        }
    }
}

data class ExploreItem(val name: String, val icon: String)

@Composable
fun HomeSearchBar(onSearchClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(56.dp)
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search for food or restaurants",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

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
                model = userPhotoUrl ?: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500&auto=format&fit=crop",
                contentDescription = "Profile",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "Deliver to", fontSize = 11.sp, color = Color.Gray)
                Text(
                    text = userName, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.White
                )
            }
        }
        
        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Outlined.Notifications,
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
    val totalMeals = plan.meals.values.sumOf { it.size }
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = plan.vendorName, fontSize = 11.sp, color = Color.LightGray)
                Text(text = plan.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            
            // Badge at bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Notifications, // Placeholder for meal icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$totalMeals meals", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
    val totalMeals = plan.meals.values.sumOf { it.size }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 250f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(text = plan.vendorName, fontSize = 13.sp, color = Color.LightGray)
                Text(text = plan.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            
            // Badge at bottom right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🍳 $totalMeals meals", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
