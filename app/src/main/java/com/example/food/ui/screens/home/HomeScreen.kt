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
            .background(Color(0xFF0A0A0A)), // Even deeper black for premium feel
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Top Header
        item {
            HomeHeader(
                userName = user?.displayName ?: "Explorer",
                userPhotoUrl = user?.photoUrl,
                onNotificationClick = onNavigateToNotifications
            )
        }

        // Search Bar
        item {
            HomeSearchBar(onSearchClick = onNavigateToSearch)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Featured Promo Banner
        item {
            FeaturedPromoBanner()
        }

        // Explore Categories
        item {
            SectionTitle("Explore Categories")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(exploreCategories) { item ->
                    ModernCategoryItem(item)
                }
            }
        }

        // Hottest Plans
        item {
            SectionHeader(title = "Hottest Plans", onActionClick = {})
            if (discoverPlansState is com.example.food.core.util.Resource.Loading) {
                // Skeleton loading would go here
            }
            
            val displayPlans = if (mealPlans.isEmpty()) getMockPlans() else mealPlans
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayPlans.take(5)) { plan ->
                    PremiumHottestCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
                }
            }
        }

        // Recommendation Section
        item {
            SectionTitle("Plans tailored for you")
        }
        
        val recommendPlans = if (mealPlans.isEmpty()) getMockPlans().reversed() else mealPlans
        items(recommendPlans) { plan ->
            ImmersiveLargeCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
        }
    }
}

fun getMockPlans() = listOf(
    MealPlan(id = "m1", name = "Bachelors Safe Haven", vendorName = "Master Chef", imageUrl = "https://images.unsplash.com/photo-1547573854-74d2a71d0827?w=800"),
    MealPlan(id = "m2", name = "Maseba's Gourmet Table", vendorName = "Abiye Briggs", imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"),
    MealPlan(id = "m3", name = "Healthy Keto Boost", vendorName = "Fit Bites", imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=800")
)

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
fun FeaturedPromoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(180.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1080&auto=format&fit=crop", // Pizza promo
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                            startX = 0f,
                            endX = 600f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart)
            ) {
                Surface(
                    color = Color(0xFFF16B24),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "NEW PROMO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Get 30% OFF\non your first plan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Order now and save big",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ModernCategoryItem(item: ExploreItem) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(75.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1A1A1A)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = item.icon, fontSize = 32.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PremiumHottestCard(plan: MealPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp)
    ) {
        Box {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 200f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFF16B24),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = plan.vendorName, fontSize = 12.sp, color = Color.LightGray)
                }
                Text(text = plan.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            
            // Meal count badge
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🍳 14 meals",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ImmersiveLargeCard(plan: MealPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(36.dp)
    ) {
        Box {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)),
                            startY = 300f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(text = "BY ${plan.vendorName.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF16B24), letterSpacing = 1.sp)
                Text(text = plan.name, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Starting from RWF 45,000", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "See all",
            fontSize = 14.sp,
            color = Color(0xFFF16B24),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}
