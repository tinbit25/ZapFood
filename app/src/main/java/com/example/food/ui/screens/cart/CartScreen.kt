package com.example.food.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.ui.viewmodel.RecommendationState
import com.example.food.domain.model.ScoredMealResponse

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    recommendationViewModel: RecommendationViewModel,
    onNavigateToCheckout: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val suggestionsState by recommendationViewModel.cartSuggestionsState.collectAsState()
    val userViewModel: com.example.food.ui.viewmodel.UserViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val user by userViewModel.user.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(cartState.meals) {
        val cartMeals = cartState.meals.map { it.first }
        if (cartMeals.isNotEmpty()) {
            recommendationViewModel.loadCartSuggestions(user?.userId ?: "guest", cartMeals)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "My Cart")

        if (cartState.meals.isEmpty() && cartState.mealPlans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your cart is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add meals from the menu to get started",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(cartState.mealPlans, key = { it.first.id }) { (plan, quantity) ->
                    CartItemCard(
                        name = plan.name,
                        price = plan.price,
                        imageUrl = plan.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMealPlan(plan) },
                        onDecrease = { cartViewModel.decreaseMealPlan(plan.id) },
                        onRemove = { cartViewModel.removeMealPlan(plan.id) }
                    )
                }

                items(cartState.meals, key = { it.first.id }) { (meal, quantity) ->
                    CartItemCard(
                        name = meal.name,
                        price = meal.price,
                        imageUrl = meal.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMeal(meal) },
                        onDecrease = { cartViewModel.decreaseMeal(meal.id) },
                        onRemove = { cartViewModel.removeMeal(meal.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    ReceiptRow("Subtotal", "ETB ${"%,.0f".format(cartState.subtotal)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Delivery & takeaway fees calculated at checkout",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Combo suggestions
                if (suggestionsState is RecommendationState.CartSuggestionsLoaded) {
                    val suggestions =
                        (suggestionsState as RecommendationState.CartSuggestionsLoaded).suggestions
                    if (suggestions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Frequently Ordered Together ✨",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
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
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                PrimaryButton(
                    text = "Proceed to Checkout  →  ETB ${"%,.0f".format(cartState.subtotal)}",
                    onClick = onNavigateToCheckout,
                    backgroundColor = colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CartItemCard(
    name: String,
    price: Double,
    imageUrl: String,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meal image
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name + price
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ETB ${"%,.0f".format(price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quantity controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .size(32.dp)
                            .background(colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.widthIn(min = 28.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(32.dp)
                            .background(colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // X button pinned to top-right corner, no background
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .offset(x = 4.dp, y = (-4).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove item",
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp)
            )
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
            .padding(vertical = 6.dp),
        color = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.mealName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = recommendation.reason,
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ Add", color = colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
