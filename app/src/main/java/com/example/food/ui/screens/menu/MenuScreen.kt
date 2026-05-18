package com.example.food.ui.screens.menu

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.SavedRecommendationRepository
import com.example.food.domain.manager.RecommendationCacheManager
import com.example.food.domain.usecase.ContextAwareRecommendationEngine
import com.example.food.domain.usecase.EthiopianBehaviorIntelligence
import com.example.food.domain.usecase.RecommendationResult
import com.example.food.domain.usecase.UserInteractionTracker
import com.example.food.ui.components.onboarding.FeatureDiscoveryHint
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.MealViewModel
import com.example.food.ui.viewmodel.PreferenceViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuScreen(
    onNavigateToAI: () -> Unit,
    onNavigateToCustom: () -> Unit,
    onNavigateToBrowse: () -> Unit,
    onNavigateToMeal: (String) -> Unit,
    onNavigateToVendor: (String) -> Unit,
    userViewModel: UserViewModel,
    recommendationViewModel: RecommendationViewModel,
    mealViewModel: MealViewModel,
    cartViewModel: CartViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val user by userViewModel.user.collectAsState()
    val userId = user?.userId ?: ""

    // Repositories & Engines
    val context = androidx.compose.ui.platform.LocalContext.current
    val savedRepo = remember { SavedRecommendationRepository() }
    val cacheManager = remember { RecommendationCacheManager(context) }
    val interactionTracker = remember { UserInteractionTracker(context) }
    val engine = remember { ContextAwareRecommendationEngine(interactionTracker) }

    // Explicit User Preferences state
    val preferencesState by preferenceViewModel.preferences.collectAsState()
    val preferences = (preferencesState as? Resource.Success)?.data ?: UserFoodPreference(userId = userId)

    // Load preferences
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            preferenceViewModel.loadPreferences(userId)
        }
    }

    // Tabs
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Smart Picks", "Saved Picks")

    // Filter Category Chip State
    var selectedFilterCategory by remember { mutableStateOf("All Picks") }

    // Meal and Bookmarks States
    val mealsResource by mealViewModel.mealsState.collectAsState()
    var savedMealIds by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch saved picks list
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            savedRepo.observeSavedMealIds(userId).collectLatest { resource ->
                if (resource is Resource.Success) {
                    savedMealIds = resource.data ?: emptyList()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Upper Title Header Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discover Hub",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Habesha Personalized Intelligence",
                    fontSize = 12.sp,
                    color = Color(0xFFF16B24),
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onNavigateToAI,
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Preferences",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Beautiful Tab Bar Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFF16B24),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFFF16B24)
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            color = if (selectedTab == index) Color.White else Color.Gray,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "tabChange"
        ) { tab ->
            when (tab) {
                0 -> {
                    // Smart Picks tab
                    SmartPicksTabContent(
                        mealsResource = mealsResource,
                        preferences = preferences,
                        savedMealIds = savedMealIds,
                        selectedFilterCategory = selectedFilterCategory,
                        onFilterCategoryChange = { selectedFilterCategory = it },
                        onNavigateToAI = onNavigateToAI,
                        onNavigateToMeal = onNavigateToMeal,
                        onNavigateToVendor = onNavigateToVendor,
                        onSaveToggle = { mealId ->
                            coroutineScope.launch {
                                if (savedMealIds.contains(mealId)) {
                                    savedRepo.unsaveMeal(userId, mealId)
                                } else {
                                    savedRepo.saveMeal(userId, mealId)
                                }
                            }
                        },
                        engine = engine,
                        cacheManager = cacheManager,
                        interactionTracker = interactionTracker,
                        userId = userId,
                        cartViewModel = cartViewModel,
                        userName = user?.displayName ?: "Habesha Customer"
                    )
                }
                1 -> {
                    // Saved Picks tab
                    SavedPicksTabContent(
                        mealsResource = mealsResource,
                        savedMealIds = savedMealIds,
                        onNavigateToMeal = onNavigateToMeal,
                        onNavigateToVendor = onNavigateToVendor,
                        onUnsave = { mealId ->
                            coroutineScope.launch {
                                savedRepo.unsaveMeal(userId, mealId)
                            }
                        },
                        cartViewModel = cartViewModel,
                        userId = userId
                    )
                }
            }
        }
    }
}

@Composable
fun SmartPicksTabContent(
    mealsResource: Resource<List<Meal>>,
    preferences: UserFoodPreference,
    savedMealIds: List<String>,
    selectedFilterCategory: String,
    onFilterCategoryChange: (String) -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToMeal: (String) -> Unit,
    onNavigateToVendor: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    engine: ContextAwareRecommendationEngine,
    cacheManager: RecommendationCacheManager,
    interactionTracker: UserInteractionTracker,
    userId: String,
    cartViewModel: CartViewModel,
    userName: String
) {
    val allMeals = (mealsResource as? Resource.Success)?.data ?: emptyList()
    val isFastingDay = EthiopianBehaviorIntelligence.isFastingDay()

    // Filter available meals only (real-time vendor status verification)
    val availableMeals = allMeals.filter { it.isAvailable }

    // Execute Client-Side Personalized Cultural Recommendation Scorer
    val rankedResults = remember(availableMeals, preferences, isFastingDay) {
        if (availableMeals.isNotEmpty()) {
            engine.scoreAndRankMeals(availableMeals, preferences, isFastingDay)
        } else {
            emptyList()
        }
    }

    // Cache meals locally if populated successfully to support offline mode
    LaunchedEffect(rankedResults) {
        if (rankedResults.isNotEmpty()) {
            cacheManager.cacheMeals("personalized_discovery", rankedResults.map { it.meal })
        }
    }

    // Get cached meals if empty (e.g. offline loading)
    val offlineMeals = remember {
        if (rankedResults.isEmpty()) {
            cacheManager.getCachedMeals("personalized_discovery") ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Determine target list to render
    val finalResults = if (rankedResults.isNotEmpty()) {
        rankedResults
    } else {
        offlineMeals.map { RecommendationResult(it, 10.0, "Saved Offline Discovery Feed") }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Section 1: Hero personalized card
        item {
            SmartRecommendationHeroCard(
                userName = userName,
                isFastingDay = isFastingDay,
                preferences = preferences
            )
        }

        // Section 2: Inline Compact Taste Preference Indicator Card
        item {
            CompactPreferenceCard(
                preferences = preferences,
                onEditClick = onNavigateToAI
            )
        }

        // Section 3: Horizontal scrollable smart category row
        item {
            SmartCategoryChipRow(
                selected = selectedFilterCategory,
                onSelectedChange = onFilterCategoryChange,
                isFastingDay = isFastingDay
            )
        }

        // Filter the recommendations depending on the clicked chip
        val filteredList = finalResults.filter { result ->
            val meal = result.meal
            when (selectedFilterCategory) {
                "Fasting Picks" -> meal.fastingFriendly || meal.veganFriendly || meal.isFastingMeal()
                "Spicy Foods" -> meal.spiceLevel == SpiceLevel.SPICY || meal.spiceLevel == SpiceLevel.VERY_SPICY
                "Traditional" -> meal.cuisineType == CuisineType.ETHIOPIAN || meal.traditionalCategory.isNotEmpty()
                "Budget Meals" -> meal.price < 200.0
                "Vegan Choices" -> meal.veganFriendly || meal.isVeganMeal()
                "Meat Lovers" -> meal.isMeatMeal()
                "Coffee Time" -> meal.name.contains("coffee", ignoreCase = true) || meal.category.contains("breakfast", ignoreCase = true)
                else -> true
            }
        }

        // Render feed or loader state
        if (mealsResource is Resource.Loading && finalResults.isEmpty()) {
            items(5) {
                RecommendationSkeletonLoader()
            }
        } else if (filteredList.isEmpty()) {
            item {
                ColdStartFallbackView(
                    allMeals = availableMeals,
                    savedMealIds = savedMealIds,
                    onNavigateToMeal = onNavigateToMeal,
                    onSaveToggle = onSaveToggle,
                    cartViewModel = cartViewModel,
                    userId = userId
                )
            }
        } else {
            // Render the items
            items(filteredList) { result ->
                SmartMealRecommendationCard(
                    result = result,
                    isSaved = savedMealIds.contains(result.meal.id),
                    onSaveToggle = {
                        onSaveToggle(result.meal.id)
                    },
                    onClick = {
                        // Track analytics interaction click in background
                        interactionTracker.trackClick(userId, result.meal.id, result.meal.category, result.meal.vendorId)
                        onNavigateToMeal(result.meal.id)
                    },
                    onQuickOrder = {
                        // Direct cart inject reorder shortcut
                        cartViewModel.addMeal(result.meal, userId)
                    }
                )
            }
        }
    }
}

@Composable
fun SavedPicksTabContent(
    mealsResource: Resource<List<Meal>>,
    savedMealIds: List<String>,
    onNavigateToMeal: (String) -> Unit,
    onNavigateToVendor: (String) -> Unit,
    onUnsave: (String) -> Unit,
    cartViewModel: CartViewModel,
    userId: String
) {
    val allMeals = (mealsResource as? Resource.Success)?.data ?: emptyList()
    val savedMeals = allMeals.filter { savedMealIds.contains(it.id) }

    if (savedMeals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "No Saved Picks",
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Saved Habesha Picks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap the bookmark icon on recommended meals to save them here for one-click reordering.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(savedMeals) { meal ->
                SmartMealRecommendationCard(
                    result = RecommendationResult(meal, 10.0, "Saved Bookmark"),
                    isSaved = true,
                    onSaveToggle = { onUnsave(meal.id) },
                    onClick = { onNavigateToMeal(meal.id) },
                    onQuickOrder = {
                        cartViewModel.addMeal(meal, userId)
                    }
                )
            }
        }
    }
}

// Section 1: Hero Card With Culturally Contextual Greetings
@Composable
fun SmartRecommendationHeroCard(
    userName: String,
    isFastingDay: Boolean,
    preferences: UserFoodPreference
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    
    val timeGreeting = when (hour) {
        in 5..11 -> "Melkam Tegat ☀️ Good Morning"
        in 12..16 -> "Melkam Kene 🌤️ Good Afternoon"
        else -> "Melkam Meshet 🌙 Good Evening"
    }

    val dynamicBackground = if (isFastingDay) {
        Brush.horizontalGradient(listOf(Color(0xFF0F5A2A), Color(0xFF1B8A44))) // Rich Green for fasting
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF7A1B0C), Color(0xFFC93B2B))) // Premium Habesha Gold/Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(dynamicBackground)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = "$timeGreeting, $userName!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFastingDay) {
                    "Today is a strict Orthodox fasting day. 🌱 We've boosted all vegan delicacies (Shiro, Misir, Gomen) to the top of your feed."
                } else {
                    "Your personal AI taste preferences are active! We've hand-picked these spicy & traditional Habesha delicacies tailored for you."
                },
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

// Section 2: Compact Preference Manager Card
@Composable
fun CompactPreferenceCard(
    preferences: UserFoodPreference,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Taste Profile Active",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🌶️ Spice: ${preferences.spicePreference.name.lowercase().replaceFirstChar { it.uppercase() }}  •  💰 Budget: ${preferences.budgetPreference}  •  🌱 Fasting: ${if (preferences.fastingMode) "ON" else "OFF"}",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Section 3: Smart Category Horizontal Scrolling Chips Row
@Composable
fun SmartCategoryChipRow(
    selected: String,
    onSelectedChange: (String) -> Unit,
    isFastingDay: Boolean
) {
    val items = remember(isFastingDay) {
        if (isFastingDay) {
            listOf("Fasting Picks", "Vegan Choices", "Budget Meals", "Traditional", "Spicy Foods", "All Picks")
        } else {
            listOf("All Picks", "Traditional", "Spicy Foods", "Budget Meals", "Vegan Choices", "Meat Lovers", "Coffee Time")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = selected == item
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color(0xFFF16B24) else Color(0xFF2A2A2A),
                modifier = Modifier.clickable { onSelectedChange(item) }
            ) {
                Text(
                    text = item,
                    color = if (isSelected) Color.White else Color.LightGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// Section 4: Premium Scored Meal Card Layout
@Composable
fun SmartMealRecommendationCard(
    result: RecommendationResult,
    isSaved: Boolean,
    onSaveToggle: () -> Unit,
    onClick: () -> Unit,
    onQuickOrder: () -> Unit
) {
    val meal = result.meal
    val interactionTracker = remember { }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (meal.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = meal.imageUrl,
                        contentDescription = meal.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍲 ZAPFOOD Delicacy", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Top Floating Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Explainability / Reason Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF16B24),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = result.explanation,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Save / Bookmark Icon Button
                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save pick",
                            tint = if (isSaved) Color(0xFFF16B24) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Price tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${meal.price.toInt()} ETB",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Description details
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meal.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", if (meal.rating > 0) meal.rating else 4.6),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "by ${if (meal.businessName.isNotEmpty()) meal.businessName else "Addis Habesha Kitchen"}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = meal.description,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color(0xFF2E2E2E))

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom actions row: ETA, Spicy level, Fasting status, and direct Checkout shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🛵 15-25 min",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (meal.fastingFriendly) "🌱 Fasting" else "🥩 Non-Fasting",
                            fontSize = 11.sp,
                            color = if (meal.fastingFriendly) Color(0xFF4CAF50) else Color(0xFFE91E63),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Direct checkout button
                    var isAdded by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            onQuickOrder()
                            isAdded = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdded) Color(0xFF4CAF50) else Color(0xFFF16B24)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isAdded) "Added! ⚡" else "Quick Order ⚡",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// Cold Start Fallback recommendations list (Trending, popular nearby, budget)
@Composable
fun ColdStartFallbackView(
    allMeals: List<Meal>,
    savedMealIds: List<String>,
    onNavigateToMeal: (String) -> Unit,
    onSaveToggle: (String) -> Unit,
    cartViewModel: CartViewModel,
    userId: String
) {
    val trending = allMeals.sortedByDescending { it.popularityScore }.take(4)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Trending Tonight in Addis Ababa 🔥",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        trending.forEach { meal ->
            SmartMealRecommendationCard(
                result = RecommendationResult(meal, 10.0, "Addis Ababa Hot Item"),
                isSaved = savedMealIds.contains(meal.id),
                onSaveToggle = { onSaveToggle(meal.id) },
                onClick = { onNavigateToMeal(meal.id) },
                onQuickOrder = {
                    cartViewModel.addMeal(meal, userId)
                }
            )
        }
    }
}

// Premium Shimmer skeleton loaders
@Composable
fun RecommendationSkeletonLoader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF2A2A2A))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(14.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
