package com.example.food.ui.screens.menu

import androidx.compose.animation.*
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "AI Plan Generator", onBackClick = onNavigateBack)

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
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Analyzing your preferences...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "We've created a custom 'Keto Explorer' plan based on your goal to lose weight.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    PrimaryButton(
                        text = "View Generated Plan",
                        onClick = { onPlanGenerated("mp1") } // Mock ID
                    )
                }
            }
        }
    }
}
