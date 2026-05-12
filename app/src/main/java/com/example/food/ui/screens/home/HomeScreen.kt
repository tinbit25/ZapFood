package com.example.food.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.foundation.BorderStroke
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
    onNavigateToSmartPreference: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVendorDiscovery: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val discoverPlansState by mealPlanViewModel.discoverPlansState.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    val recommendationState by recommendationViewModel.uiState.collectAsState()
    
    val mealPlans = (discoverPlansState as? Resource.Success)?.data ?: emptyList()
    val meals = (mealsState as? Resource.Success)?.data ?: emptyList()
    
    val colorScheme = MaterialTheme.colorScheme

    val foodFilters = listOf(
        FoodFilter("Fasting", "🌱", com.example.food.data.model.FoodType.FASTING, null),
        FoodFilter("Meat", "🍖", com.example.food.data.model.FoodType.NON_FASTING, com.example.food.data.model.DietType.MEAT),
        FoodFilter("Vegan", "🥬", null, com.example.food.data.model.DietType.VEGAN)
    )

    var selectedFilter by remember { mutableStateOf<FoodFilter?>(null) }
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
            .background(colorScheme.background),
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
        }

        // Ethiopian Classification Filters
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(foodFilters) { filter ->
                    FilterChip(
                        filter = filter,
                        isSelected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = if (selectedFilter == filter) null else filter
                            mealViewModel.updateFilters(
                                mealViewModel.currentFilters.value.copy(
                                    foodType = selectedFilter?.foodType,
                                    dietType = selectedFilter?.dietType
                                )
                            )
                        }
                    )
                }
            }
        }

        // Discovery Center
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discovery Center",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.example.food.ui.components.onboarding.FeatureDiscoveryHint(
                        text = "Find restaurants nearby",
                        icon = "📍",
                        modifier = Modifier.weight(1f)
                    )
                    com.example.food.ui.components.onboarding.FeatureDiscoveryHint(
                        text = "Track your order live",
                        icon = "🛵",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                var showTooltip by remember { mutableStateOf(true) }
                com.example.food.ui.components.onboarding.ContextualTooltip(
                    text = "New: Personalized AI picks based on your fasting habits!",
                    isVisible = showTooltip,
                    onDismiss = { showTooltip = false },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        // Smart Hybrid Fasting Prompt
        if (showFastingPrompt) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF1E3A2F) else Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Today is a fasting day",
                                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Looking for the best fasting stews in the city?",
                            color = if (isSystemInDarkTheme()) Color.LightGray else Color(0xFF555555),
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
                                Text("Show Me")
                            }
                            TextButton(onClick = { showFastingPrompt = false }) {
                                Text("Maybe later", color = if (isSystemInDarkTheme()) Color.LightGray else Color.Gray)
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

        // Recommendations Hub Entry
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clickable { onNavigateToSmartPreference() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Smart Picks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                        Text(text = "Personalized Ethiopian favorites", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
                }
            }
        }

        // Smart Picks Logic
        if (recommendationState is RecommendationState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }
        } else if (recommendationState is RecommendationState.Success) {
            val recData = recommendationState as RecommendationState.Success
            
            if (recData.smartPicks.isNotEmpty()) {
                item {
                    SectionHeader(title = "Smart Picks For You ✨", onActionClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recData.smartPicks) { scoredMeal ->
                            PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                        }
                    }
                }
            }

            if (recData.fastingMeals.isNotEmpty()) {
                item {
                    SectionHeader(title = "Today's Fasting Specials 🌱", onActionClick = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recData.fastingMeals) { scoredMeal ->
                            PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Popular in Addis Ababa \uD83D\uDCCD", onActionClick = {})
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recData.popularInAddis) { scoredMeal ->
                        PopularMealCard(scoredMeal = scoredMeal, onClick = { onNavigateToDetails(scoredMeal.mealId) })
                    }
                }
            }
        } else {
            item {
                SectionHeader(title = "Recommended Ethiopian Meals", onActionClick = {})
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
    }
}

@Composable
fun PopularMealCard(scoredMeal: ScoredMealResponse, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(colorScheme.surface.copy(alpha = 0.5f))) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                    tint = colorScheme.primary.copy(alpha = 0.5f)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${scoredMeal.score.toInt()}% Match",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = scoredMeal.mealName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scoredMeal.reason,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PopularMealCard(meal: Meal, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
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
                Text(text = meal.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, maxLines = 1)
                Text(text = "ETB ${"%,.0f".format(meal.price * 1000)}", fontSize = 12.sp, color = colorScheme.primary)
            }
        }
    }
}

@Composable
fun FilterChip(
    filter: FoodFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) colorScheme.primary else colorScheme.surfaceVariant,
        border = if (isSelected) null else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = filter.icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = filter.name,
                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun HomeSearchBar(onSearchClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(56.dp)
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search for food or restaurants",
                color = colorScheme.onSurfaceVariant,
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
    val colorScheme = MaterialTheme.colorScheme
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
                Text(text = "Deliver to", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                Text(
                    text = userName, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = colorScheme.onBackground
                )
            }
        }
        
        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = colorScheme.onBackground,
                    modifier = Modifier.size(28.dp)
                )
            }
            Surface(
                color = colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "1", fontSize = 10.sp, color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
fun SectionHeader(title: String, onActionClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
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
            color = colorScheme.onBackground
        )
        Text(
            text = "See all",
            fontSize = 14.sp,
            color = colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

data class FoodFilter(
    val name: String, 
    val icon: String, 
    val foodType: com.example.food.data.model.FoodType?, 
    val dietType: com.example.food.data.model.DietType?
)
