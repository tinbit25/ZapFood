package com.example.food.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.PreferenceChip
import com.example.food.ui.components.PrimaryButton

@Composable
fun PreferencesOnboardingScreen(
    onComplete: (List<String>, List<String>) -> Unit
) {
    var selectedGoals by remember { mutableStateOf(setOf<String>()) }
    var selectedDiets by remember { mutableStateOf(setOf<String>()) }

    val goals = listOf("Weight Loss", "Muscle Gain", "Healthy Eating", "Better Sleep", "More Energy")
    val diets = listOf("Vegan", "Keto", "Paleo", "Low Carb", "Gluten Free", "Dairy Free")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Tell us about yourself",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select your health goals and dietary preferences to personalize your meal plans.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Health Goals",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Manual chip grid - 2 per row (avoids FlowRow API version issues)
        goals.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { goal ->
                    Box(modifier = Modifier.weight(1f)) {
                        PreferenceChip(
                            text = goal,
                            isSelected = selectedGoals.contains(goal),
                            onSelectionChange = { isSelected ->
                                selectedGoals = if (isSelected) selectedGoals + goal else selectedGoals - goal
                            }
                        )
                    }
                }
                // Fill remaining space if odd number of items
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dietary Preferences",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        diets.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { diet ->
                    Box(modifier = Modifier.weight(1f)) {
                        PreferenceChip(
                            text = diet,
                            isSelected = selectedDiets.contains(diet),
                            onSelectionChange = { isSelected ->
                                selectedDiets = if (isSelected) selectedDiets + diet else selectedDiets - diet
                            }
                        )
                    }
                }
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Continue",
            onClick = { onComplete(selectedGoals.toList(), selectedDiets.toList()) },
            enabled = selectedGoals.isNotEmpty() || selectedDiets.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

