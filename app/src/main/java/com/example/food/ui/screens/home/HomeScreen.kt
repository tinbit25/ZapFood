package com.example.food.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.data.model.MealPlan
import com.example.food.data.model.Meal
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
    
    val categories = listOf(
        Pair("Burger", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop"),
        Pair("Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop"),
        Pair("Sushi", "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop"),
        Pair("Dessert", "https://images.unsplash.com/photo-1551024601-bec78aea704b?w=500&auto=format&fit=crop")
    )

    var showMPCodeDialog by remember { mutableStateOf(false) }

    if (showMPCodeDialog) {
        MPCodeDialog(
            onDismiss = { showMPCodeDialog = false },
            onConfirm = { code ->
                showMPCodeDialog = false
                // In real app, fetch plan by code. Here we'll just go to details of mp1
                onNavigateToMealPlanDetails("mp1")
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            HeaderSection(
                userName = user?.displayName ?: "Guest",
                userPhotoUrl = user?.photoUrl,
                onSearchClick = onNavigateToSearch
            )
        }
        
        item {
            CategoriesSection(categories = categories)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalized Meal Plans",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Paste Code",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showMPCodeDialog = true }
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mealPlans) { plan ->
                    MealPlanCard(plan = plan, onClick = { onNavigateToMealPlanDetails(plan.mealPlanId) })
                }
            }
        }

        item {
            SectionTitle("Trending Meals")
        }
        
        // Mocking some individual meals for the trending section
        items(mealPlans.flatMap { it.meals }.distinctBy { it.mealId }.take(5)) { meal ->
            MealRow(meal = meal, onClick = { onNavigateToDetails(meal.mealId) })
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun HeaderSection(
    userName: String,
    userPhotoUrl: String?,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Kravinz", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(text = "Hello, $userName", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            AsyncImage(
                model = userPhotoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500&auto=format&fit=crop",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.clickable { onSearchClick() }) {
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Search meal plans or restaurants",
                leadingIcon = Icons.Default.Search,
                enabled = false
            )
        }
    }
}

@Composable
fun MealPlanCard(plan: MealPlan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = plan.mealPlanName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = plan.mealPlanName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = plan.type.name, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    Text(text = " • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = plan.vendorName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "$${plan.price}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun MealRow(meal: Meal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = meal.mealName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = meal.mealName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = meal.vendorName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "$${meal.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CategoriesSection(categories: List<Pair<String, String>>) {
    Column {
        SectionTitle("Categories")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = category.second,
                        contentDescription = category.first,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = category.first, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
