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
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.data.model.Meal
import com.example.food.data.model.MealPlan

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    onNavigateToCheckout: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    
    val deliveryFee = 2.99
    val total = cartState.subtotal + deliveryFee

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "My Cart")

        if (cartState.meals.isEmpty() && cartState.mealPlans.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Your cart is empty", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                items(cartState.mealPlans) { (plan, quantity) ->
                    CartItemRow(
                        name = plan.mealPlanName,
                        price = plan.price,
                        imageUrl = plan.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMealPlan(plan) },
                        onDecrease = { /* Implement decrease in VM if needed */ }
                    )
                }

                items(cartState.meals) { (meal, quantity) ->
                    CartItemRow(
                        name = meal.mealName,
                        price = meal.price,
                        imageUrl = meal.imageUrl,
                        quantity = quantity,
                        onIncrease = { cartViewModel.addMeal(meal) },
                        onDecrease = { /* Implement decrease in VM if needed */ }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(24.dp))

                    ReceiptRow("Subtotal", "$${"%.2f".format(cartState.subtotal)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow("Delivery Fee", "$${"%.2f".format(deliveryFee)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReceiptRow("Total", "$${"%.2f".format(total)}", isTotal = true)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = "Checkout ($${"%.2f".format(total)})",
                    onClick = onNavigateToCheckout
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "$${price}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(text = quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = onIncrease,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, amount: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            fontSize = if (isTotal) 18.sp else 14.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
