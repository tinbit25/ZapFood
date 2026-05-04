package com.example.food.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.data.model.MealPlan
import com.example.food.data.model.Meal

@Composable
fun MealPlanDetailsScreen(
    planId: String,
    mealPlanViewModel: MealPlanViewModel,
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit
) {
    val mealPlans by mealPlanViewModel.mealPlans.collectAsState()
    val plan = mealPlans.find { it.mealPlanId == planId }

    if (plan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopNavBar(title = "Meal Plan Details", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = plan.imageUrl,
                contentDescription = plan.mealPlanName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = plan.mealPlanName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${plan.type.name} Plan • ${plan.vendorName}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(text = "$${plan.price}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(text = "Nutritional Summary (Daily Average)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                NutritionalSummaryRow(plan.nutritionalSummary.totalCalories, plan.nutritionalSummary.totalProteins, plan.nutritionalSummary.totalCarbs, plan.nutritionalSummary.totalFats)

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(text = "Included Meals", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                plan.meals.forEach { meal ->
                    MealItemCard(meal)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Box(modifier = Modifier.padding(24.dp)) {
            PrimaryButton(
                text = "Purchase Plan ($${plan.price})",
                onClick = { 
                    cartViewModel.addMealPlan(plan)
                    onNavigateBack() // Navigate back after adding
                }
            )
        }
    }
}

@Composable
fun NutritionalSummaryRow(calories: Int, protein: Float, carbs: Float, fat: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NutrientItem("Calories", "$calories kcal")
        NutrientItem("Protein", "${protein}g")
        NutrientItem("Carbs", "${carbs}g")
        NutrientItem("Fat", "${fat}g")
    }
}

@Composable
fun NutrientItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MealItemCard(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = meal.imageUrl,
                contentDescription = meal.mealName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = meal.mealName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "${meal.calories} kcal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
