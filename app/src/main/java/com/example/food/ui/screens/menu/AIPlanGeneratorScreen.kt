package com.example.food.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.MealPlanViewModel
import kotlinx.coroutines.delay

@Composable
fun AIPlanGeneratorScreen(
    mealPlanViewModel: MealPlanViewModel,
    onPlanGenerated: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var isGenerating by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(3000) // Simulate AI processing
        isGenerating = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "AI Generator", onBackClick = onNavigateBack)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = androidx.compose.ui.graphics.Color(0xFFF16B24),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Generating your plan...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "Crafting a personalized meal plan just for you.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Plan Ready!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFF16B24)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "We've created a personalized plan based on your health goals and dietary preferences.",
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.LightGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    PrimaryButton(
                        text = "View Generated Plan",
                        onClick = { onPlanGenerated("mp1") },
                        backgroundColor = androidx.compose.ui.graphics.Color(0xFFF16B24)
                    )
                }
            }
        }
    }
}
