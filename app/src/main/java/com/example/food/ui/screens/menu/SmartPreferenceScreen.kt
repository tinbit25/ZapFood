package com.example.food.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.PreferenceViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPreferenceScreen(
    onNavigateBack: () -> Unit,
    onPreferencesSaved: () -> Unit,
    userViewModel: UserViewModel,
    recommendationViewModel: RecommendationViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val userId = user?.userId ?: ""
    val preferencesState by preferenceViewModel.preferences.collectAsState()
    
    // Preference State
    var fastingMode by remember { mutableStateOf(false) }
    var selectedSpice by remember { mutableStateOf(SpiceLevel.MEDIUM) }
    var favoriteFoods by remember { mutableStateOf("") }
    var selectedBudget by remember { mutableStateOf("STANDARD") }
    var dietaryType by remember { mutableStateOf("ANY") }
    var preferredMealTime by remember { mutableStateOf("ANY") }
    
    var hasLoadedInitialData by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            preferenceViewModel.loadPreferences(userId)
        }
    }

    LaunchedEffect(preferencesState) {
        if (preferencesState is Resource.Success) {
            val prefs = (preferencesState as Resource.Success<UserFoodPreference>).data
            if (prefs != null) {
                fastingMode = prefs.fastingMode
                selectedSpice = prefs.spicePreference
                favoriteFoods = prefs.favoriteFoods.joinToString(", ")
                selectedBudget = prefs.budgetPreference
                dietaryType = prefs.dietaryType
                preferredMealTime = prefs.preferredMealTime
            }
            hasLoadedInitialData = true
        } else if (preferencesState is Resource.Error) {
            hasLoadedInitialData = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        TopNavBar(
            title = "Smart Preferences",
            onBackClick = onNavigateBack
        )

        if (!hasLoadedInitialData && userId.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF16B24))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Personalize Your Taste",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "We'll tailor your smart picks to match your cultural habits.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Fasting Mode
                PreferenceSection(title = "Fasting Adherence") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Strict Fasting Mode", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("Only show fasting/vegan meals during fasting periods.", color = Color.Gray, fontSize = 12.sp)
                        }
                        Switch(
                            checked = fastingMode,
                            onCheckedChange = { fastingMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF16B24), checkedTrackColor = Color(0xFFF16B24).copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Spice Preference
                PreferenceSection(title = "Spice Level") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpiceLevel.entries.chunked(3).forEach { rowSpices ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowSpices.forEach { spice ->
                                    FilterChip(
                                        selected = selectedSpice == spice,
                                        onClick = { selectedSpice = spice },
                                        label = { Text(spice.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFF16B24),
                                            selectedLabelColor = Color.White,
                                            labelColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Favorite Foods
                PreferenceSection(title = "Favorite Ethiopian Foods") {
                    CustomTextField(
                        value = favoriteFoods,
                        onValueChange = { favoriteFoods = it },
                        placeholder = "e.g. Shiro, Tibs, Kitfo",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Budget Preference
                PreferenceSection(title = "Budget Preference") {
                    val budgets = listOf("BUDGET", "STANDARD", "PREMIUM")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        budgets.forEach { budget ->
                            FilterChip(
                                selected = selectedBudget == budget,
                                onClick = { selectedBudget = budget },
                                label = { Text(budget.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFF16B24),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.Gray
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = if (isSaving) "Saving..." else "Update Smart Picks ✨",
                    onClick = {
                        val uid = user?.userId ?: return@PrimaryButton
                        isSaving = true
                        val prefs = UserFoodPreference(
                            userId = uid,
                            fastingMode = fastingMode,
                            spicePreference = selectedSpice,
                            budgetPreference = selectedBudget,
                            dietaryType = dietaryType,
                            favoriteFoods = favoriteFoods.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            preferredMealTime = preferredMealTime,
                            lastUpdated = System.currentTimeMillis()
                        )
                        
                        recommendationViewModel.saveUserPreferences(prefs) { success ->
                            isSaving = false
                            if (success) {
                                onPreferencesSaved()
                            }
                        }
                    },
                    enabled = !isSaving,
                    backgroundColor = Color(0xFFF16B24)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun PreferenceSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
