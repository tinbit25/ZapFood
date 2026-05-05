package com.example.food.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AIViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel

@Composable
fun AIPlanGeneratorScreen(
    onPlanGenerated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    aiViewModel: AIViewModel = viewModel(),
    mealPlanViewModel: MealPlanViewModel,
    userViewModel: com.example.food.ui.viewmodel.UserViewModel = viewModel()
) {
    val aiPlanState by aiViewModel.aiPlanState.collectAsState()
    val user by userViewModel.user.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) } // 1: Form, 2: Loading/Result
    var isSaving by remember { mutableStateOf(false) }
    
    // Form State
    var dietaryPref by remember { mutableStateOf("") }
    var calorieTarget by remember { mutableStateOf("2000") }
    var selectedGoal by remember { mutableStateOf(HealthGoal.MAINTENANCE) }
    var selectedDuration by remember { mutableStateOf(DurationType.WEEKLY) }
    var mealsPerDay by remember { mutableStateOf("3") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        TopNavBar(
            title = if (currentStep == 1) "AI Preferences" else "Generated Plan", 
            onBackClick = {
                if (currentStep == 2) currentStep = 1 else onNavigateBack()
            }
        )

        if (currentStep == 1) {
            AIPreferenceForm(
                dietaryPref = dietaryPref,
                onDietaryChange = { dietaryPref = it },
                calorieTarget = calorieTarget,
                onCalorieChange = { calorieTarget = it },
                selectedGoal = selectedGoal,
                onGoalChange = { selectedGoal = it },
                mealsPerDay = mealsPerDay,
                onMealsChange = { mealsPerDay = it },
                onGenerate = {
                    val prefs = AIPreference(
                        userId = user?.userId ?: "guest",
                        dietaryPreferences = listOf(dietaryPref).filter { it.isNotEmpty() },
                        allergies = emptyList(),
                        calorieTarget = calorieTarget.toIntOrNull() ?: 2000,
                        goal = selectedGoal,
                        mealsPerDay = mealsPerDay.toIntOrNull() ?: 3,
                        duration = selectedDuration
                    )
                    aiViewModel.generatePlan(prefs)
                    currentStep = 2
                }
            )
        } else {
            AIResultView(
                aiPlanState = aiPlanState,
                isSaving = isSaving,
                onSave = { plan ->
                    user?.let { currentUser ->
                        isSaving = true
                        mealPlanViewModel.selectPlan(plan)
                        mealPlanViewModel.savePlan(currentUser) { result ->
                            isSaving = false
                            if (result is Resource.Success) {
                                onPlanGenerated(plan.id)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AIPreferenceForm(
    dietaryPref: String,
    onDietaryChange: (String) -> Unit,
    calorieTarget: String,
    onCalorieChange: (String) -> Unit,
    selectedGoal: HealthGoal,
    onGoalChange: (HealthGoal) -> Unit,
    mealsPerDay: String,
    onMealsChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Personalize your AI Plan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text("Tell us your goals and we'll craft the perfect menu.", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        Text("Dietary Preference", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        CustomTextField(
            value = dietaryPref,
            onValueChange = onDietaryChange,
            placeholder = "e.g. Vegan, Halal, Keto",
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Health Goal", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HealthGoal.entries.forEach { goal ->
                FilterChip(
                    selected = selectedGoal == goal,
                    onClick = { onGoalChange(goal) },
                    label = { Text(goal.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF16B24),
                        selectedLabelColor = Color.White,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Calories", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                CustomTextField(
                    value = calorieTarget,
                    onValueChange = onCalorieChange,
                    placeholder = "2000",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Meals/Day", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                CustomTextField(
                    value = mealsPerDay,
                    onValueChange = onMealsChange,
                    placeholder = "3",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Generate My Plan ✨",
            onClick = onGenerate,
            backgroundColor = Color(0xFFF16B24)
        )
    }
}

@Composable
fun AIResultView(
    aiPlanState: Resource<MealPlan>,
    isSaving: Boolean,
    onSave: (MealPlan) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (aiPlanState) {
            is Resource.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFF16B24))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is mapping your meals...", color = Color.White)
                }
            }
            is Resource.Error -> {
                Text("Error: ${aiPlanState.message}", color = Color.Red)
            }
            is Resource.Success -> {
                val plan = aiPlanState.data
                if (plan != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Plan Ready!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF16B24))
                                Text(plan.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(plan.description, fontSize = 14.sp, color = Color.Gray)
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    InfoTag(icon = Icons.Default.Bolt, text = "${plan.nutritionalSummary.totalCalories} kcal")
                                    InfoTag(icon = Icons.Default.Restaurant, text = "AI Curated")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Generated Schedule Preview", color = Color.White, fontWeight = FontWeight.Bold)
                        
                        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                            items(plan.meals.keys.toList().take(3)) { day ->
                                Text(day.name, color = Color(0xFFF16B24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Meals mapped successfully", color = Color.Gray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        PrimaryButton(
                            text = if (isSaving) "Saving..." else "Save & Open Plan",
                            onClick = { onSave(plan) },
                            enabled = !isSaving,
                            backgroundColor = Color(0xFFF16B24)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFF16B24), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}
