package com.example.food.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.data.model.SpiceLevel
import com.example.food.ui.viewmodel.PreferenceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceOnboardingScreen(
    userId: String,
    viewModel: PreferenceViewModel,
    onOnboardingComplete: () -> Unit
) {
    var selectedSpice by remember { mutableStateOf(SpiceLevel.MEDIUM) }
    var fastingPreference by remember { mutableStateOf(false) }
    var budgetRange by remember { mutableStateOf("Medium") }
    
    // Simplistic string list for MVP onboarding
    val popularMeals = listOf("Shiro", "Doro Wat", "Kitfo", "Tibs", "Beyaynetu", "Firfir")
    var selectedMeals by remember { mutableStateOf(setOf<String>()) }

    val status by viewModel.onboardingStatus.collectAsState()

    LaunchedEffect(status) {
        if (status is Resource.Success) {
            viewModel.resetOnboardingStatus()
            onOnboardingComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to Habesha Foods 🍲", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Let's personalize your experience.", color = Color.LightGray, fontSize = 16.sp)

            // Fasting
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Do you observe fasting days?", color = Color.White, modifier = Modifier.weight(1f))
                Switch(
                    checked = fastingPreference,
                    onCheckedChange = { fastingPreference = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF16B24), checkedTrackColor = Color(0x80F16B24))
                )
            }

            // Spice Level
            Column {
                Text("Preferred Spice Level", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpiceLevel.values().forEach { level ->
                        val isSelected = selectedSpice == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Color(0xFFF16B24) else Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .clickable { selectedSpice = level }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(level.name, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Budget
            Column {
                Text("Preferred Budget Range", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { budget ->
                        val isSelected = budgetRange == budget
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) Color(0xFFF16B24) else Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .clickable { budgetRange = budget }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(budget, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Favorite Meals
            Column {
                Text("Select your favorites (Optional)", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    popularMeals.forEach { meal ->
                        val isSelected = selectedMeals.contains(meal)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFF16B24) else Color(0xFF2A2A2A),
                            modifier = Modifier.clickable {
                                selectedMeals = if (isSelected) selectedMeals - meal else selectedMeals + meal
                            }
                        ) {
                            Text(
                                text = meal,
                                color = if (isSelected) Color.White else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveOnboardingPreferences(
                        userId = userId,
                        spiceLevel = selectedSpice,
                        fastingMode = fastingPreference,
                        favoriteMeals = selectedMeals.toList(),
                        budgetRange = budgetRange
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF16B24))
            ) {
                if (status is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save & Continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            TextButton(
                onClick = onOnboardingComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for now", color = Color.Gray)
            }
        }
    }
}
