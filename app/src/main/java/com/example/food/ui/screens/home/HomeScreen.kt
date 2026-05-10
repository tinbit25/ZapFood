package com.example.food.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restaurant
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
import com.example.food.ui.viewmodel.MealViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.ui.viewmodel.RecommendationState
import com.example.food.domain.model.ScoredMealResponse
import com.example.food.R
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Star
import com.example.food.data.model.MealPlan
import com.example.food.data.model.Meal
import com.example.food.ui.components.MPCodeDialog
import com.example.food.core.util.Resource
import com.example.food.domain.usecase.EthiopianBehaviorIntelligence

@Composable
fun HomeScreen(
    userViewModel: UserViewModel,
    mealPlanViewModel: MealPlanViewModel,
    mealViewModel: MealViewModel,
    recommendationViewModel: RecommendationViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToMealPlanDetails: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val discoverPlansState by mealPlanViewModel.discoverPlansState.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    val recommendationState by recommendationViewModel.uiState.collectAsState()
    
    val mealPlans = (discoverPlansState as? Resource.Success)?.data ?: emptyList()
    val meals = (mealsState as? Resource.Success)?.data ?: emptyList()
    
    val exploreCategories = listOf(
        ExploreItem("Restaurants", "🍟"),
        ExploreItem("ZapFood", "🥘"),
        ExploreItem("Chefs", "👩‍🍳"),
        ExploreItem("Cafes", "☕")
    )

    var showFastingPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (EthiopianBehaviorIntelligence.isFastingDay()) {
            showFastingPrompt = true
        }
        mealPlanViewModel.fetchDiscoverPlans()
        mealViewModel.fetchMeals()
        user?.let { recommendationViewModel.loadHomeRecommendations(it.userId) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
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

        // Smart Hybrid Fasting Prompt
        if (showFastingPrompt) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2F))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Today is a fasting day",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Would you like fasting meal recommendations?",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { 
                                    showFastingPrompt = false 
                                    mealViewModel.applyCategory(com.example.food.data.model.EthiopianFoodCategory.FASTING_FOODS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Yes")
                            }
                            TextButton(onClick = { showFastingPrompt = false }) {
                                Text("No, thanks", color = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        // Featured Promo Banner
        item {
            FeaturedPromoBanner()
        }

        // MPCode Import Section
        item {
            val rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val importState by rewardViewModel.importState.collectAsState()
            var showImportDialog by remember { mutableStateOf(false) }

            LaunchedEffect(importState) {
                if (importState is Resource.Success) {
                    onNavigateToMealPlanDetails((importState as Resource.Success).data!!.id)
                }
            }

            if (showImportDialog) {
                MPCodeDialog(
                    onDismiss = { showImportDialog = false },
                    onConfirm = { code -> 
                        user?.let { rewardViewModel.importPlan(it, code) }
                    },
                    isLoading = importState is Resource.Loading,
                    errorMessage = (importState as? Resource.Error)?.message
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { showImportDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFFF16B24).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFF16B24))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Have a share code?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Import a meal plan using MPCode", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color.Gray)
                }
            }
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
            if (discoverPlansState is Resource.Loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF16B24))
                }
            } else if (mealPlans.isEmpty()) {
                Text(
                    text = "No plans available. Ask Admin to seed data!",
                    modifier = Modifier.padding(24.dp),
                    color = Color.Gray
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(mealPlans) { plan ->
                        PremiumHottestCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
                    }
                }
            }
        }

        // AI Recommendation Sections
        if (recommendationState is RecommendationState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF16B24))
                }
            }
        } else if (recommendationState is RecommendationState.Success) {
            val recData = recommendationState as RecommendationState.Success
            
            // Recommended For You
            if (recData.personalized.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recommended For You \uD83C\uDFAF", onActionClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recData.personalized) { scoredMeal ->
                            PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                        }
                    }
                }
            }
            
            // Trending
            if (recData.trending.isNotEmpty()) {
                item {
                    SectionHeader(title = "Trending Ethiopian Meals \uD83D\uDD25", onActionClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recData.trending) { scoredMeal ->
                            PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                        }
                    }
                }
            }
            
            // Fasting Picks
            if (recData.fastingPicks.isNotEmpty()) {
                item {
                    SectionHeader(title = "Fasting Picks \uD83C\uDF3F", onActionClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recData.fastingPicks) { scoredMeal ->
                            PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                        }
                    }
                }
            }
        } else if (recommendationState is RecommendationState.Error) {
            // Fallback to local meals if API fails
            item {
                SectionHeader(title = "Popular Meals", onActionClick = {})
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(meals) { meal ->
                        PopularMealCard(meal = meal, onClick = { onNavigateToDetails(meal.id) })
                    }
                }
            }
        }

        // Recommendation Section
        item {
            SectionTitle("Tailored for you")
        }
        
        if (mealPlans.isNotEmpty()) {
            items(mealPlans.reversed()) { plan ->
                ImmersiveLargeCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.id) })
            }
        }
    }
}

@Composable
fun PopularMealCard(scoredMeal: ScoredMealResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                AsyncImage(
                    model = scoredMeal.imageUrl,
                    contentDescription = scoredMeal.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Score Badge
                if (scoredMeal.matchScore > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${(scoredMeal.matchScore * 100).toInt()}% Match",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = scoredMeal.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "4.8", fontSize = 12.sp, color = Color.Gray) // Stub rating for now
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ETB ${scoredMeal.price.toInt()}", fontSize = 12.sp, color = Color(0xFFF16B24), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scoredMeal.reason,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun PopularMealCard(meal: Meal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = meal.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(text = "ETB ${"%,.0f".format(meal.price * 1000)}", fontSize = 12.sp, color = Color(0xFFF16B24))
            }
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
                imageVector = Icons.Default.Search,
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
                model = "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1080&auto=format&fit=crop",
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
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color(0xFFF16B24),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = plan.vendorName, fontSize = 12.sp, color = Color.LightGray)
                }
                Text(text = plan.name, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
                    Text(text = "Starting from ETB 45,000", fontSize = 12.sp, color = Color.LightGray)
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
