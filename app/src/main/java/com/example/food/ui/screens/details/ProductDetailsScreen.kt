package com.example.food.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.data.model.Meal
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.MealViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.core.util.Resource

@Composable
fun ProductDetailsScreen(
    productId: String,
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    mealViewModel: MealViewModel = viewModel(),
    recommendationViewModel: RecommendationViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    var quantity by remember { mutableIntStateOf(1) }
    
    // In a real app, we would have a fetchMealById in ViewModel
    // For now, we find it in the current list or fallback to mock if list is empty
    val product = (mealsState as? Resource.Success)?.data?.find { it.id == productId } ?: Meal(
        id = productId,
        name = "Classic Cheeseburger",
        description = "Juicy beef patty with cheese and premium ingredients.",
        price = 8.99,
        imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800&auto=format&fit=crop",
        vendorId = "mock-vendor",
        vendorName = "Burger King"
    )

    LaunchedEffect(productId) {
        user?.let { recommendationViewModel.trackAnalyticsEvent(it.userId, "view_meal", productId, context = "product_details") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(
            title = "Meal Details",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "ETB ${"%,.0f".format(product.price * 1000)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF16B24)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Delicious ${product.name} prepared by ${product.vendorName} with premium ingredients.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
        
        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White)
                }
                Text(
                    text = quantity.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF16B24), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            PrimaryButton(
                text = "Add to Cart",
                onClick = { 
                    repeat(quantity) {
                        cartViewModel.addMeal(product) 
                    }
                    user?.let { recommendationViewModel.trackAnalyticsEvent(it.userId, "add_to_cart", productId, context = "product_details") }
                    onNavigateBack()
                },
                modifier = Modifier.weight(1f),
                backgroundColor = Color(0xFFF16B24)
            )
        }
    }
}
