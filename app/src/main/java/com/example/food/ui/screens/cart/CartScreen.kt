package com.example.food.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
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
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.ui.viewmodel.RecommendationState
import com.example.food.data.model.Meal
import com.example.food.data.model.MealPlan
import com.example.food.domain.model.ScoredMealResponse
import com.example.food.R

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    recommendationViewModel: RecommendationViewModel,
    onNavigateToCheckout: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val suggestionsState by recommendationViewModel.cartSuggestionsState.collectAsState()
    val userViewModel: com.example.food.ui.viewmodel.UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val user by userViewModel.user.collectAsState()
    
    val colorScheme = MaterialTheme.colorScheme
    
    LaunchedEffect(cartState.meals) {
        val cartMeals = cartState.meals.map { it.first }
        if (cartMeals.isNotEmpty()) {
            recommendationViewModel.loadCartSuggestions(user?.userId ?: "guest", cartMeals)
        }
    }
    
    val deliveryFee = 2000.0 // ETB
    val total = (cartState.subtotal * 1000) + deliveryFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "My Cart")

        if (cartState.meals.isEmpty() && cartState.mealPlans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Your cart is empty", fontSize = 18.sp, color = colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                items(cartState.mealPlans) { (plan, quantity) ->
                    CartItemRow(
                        name = plan.name,
                        price = plan.price,
                        imageUrl = plan.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMealPlan(plan) },
                        onDecrease = { /* Implement decrease in VM */ }
                    )
                }

                items(cartState.meals) { (meal, quantity) ->
                    CartItemRow(
                        name = meal.name,
                        price = meal.price,
                        imageUrl = meal.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMeal(meal) },
                        onDecrease = { /* Implement decrease in VM */ }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(24.dp))

                    ReceiptRow("Subtotal", "ETB ${"%,.0f".format(cartState.subtotal * 1000)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow("Delivery Fee", "ETB ${"%,.0f".format(deliveryFee)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow("Total", "ETB ${"%,.0f".format(total)}", isTotal = true)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                if (suggestionsState is RecommendationState.CartSuggestionsLoaded) {
                    val suggestions = (suggestionsState as RecommendationState.CartSuggestionsLoaded).suggestions
                    if (suggestions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Frequently Ordered Together ✨",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        items(suggestions) { combo ->
                            ComboRecommendationCard(
                                recommendation = combo,
                                onAddClick = { 
                                    val dummyMeal = com.example.food.data.model.Meal(
                                        id = combo.mealId,
                                        name = combo.mealName,
                                        price = 0.0,
                                        imageUrl = "",
                                        vendorId = "vendor_id",
                                        category = "traditional",
                                        tags = emptyList(),
                                        ingredients = emptyList(),
                                        spiceLevel = com.example.food.data.model.SpiceLevel.MEDIUM,
                                        fastingFriendly = false,
                                        veganFriendly = false,
                                        popularityScore = 50.0
                                    )
                                    cartViewModel.addMeal(dummyMeal) 
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = "Checkout | ETB ${"%,.0f".format(total)}",
                    onClick = onNavigateToCheckout,
                    backgroundColor = colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    name: String,
    price: Double,
    imageUrl: String,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(text = "ETB ${"%,.0f".format(price * 1000)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = colorScheme.onSurface)
                }
                Text(text = quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorScheme.primary, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, amount: String, isTotal: Boolean = false) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) colorScheme.onBackground else colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) colorScheme.primary else colorScheme.onBackground
        )
    }
}

@Composable
fun ComboRecommendationCard(
    recommendation: ScoredMealResponse,
    onAddClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recommendation.mealName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(text = recommendation.reason, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, maxLines = 2)
            }
            
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = "Add", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
