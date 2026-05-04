package com.example.food.ui.screens.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.ui.components.TopNavBar

@Composable
fun MenuScreen(
    onNavigateToAI: () -> Unit,
    onNavigateToCustom: () -> Unit,
    onNavigateToBrowse: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopNavBar(title = "Meal Planning Hub")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Choose your path",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "How would you like to plan your meals today?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            HubCard(
                title = "AI Plan Generator",
                description = "Let our AI craft a plan based on your health goals.",
                icon = Icons.Default.AutoAwesome,
                onClick = onNavigateToAI
            )

            Spacer(modifier = Modifier.height(16.dp))

            HubCard(
                title = "Create Custom Plan",
                description = "Pick and mix meals from different vendors.",
                icon = Icons.Default.Build,
                onClick = onNavigateToCustom
            )

            Spacer(modifier = Modifier.height(16.dp))

            HubCard(
                title = "Browse Vendor Plans",
                description = "Explore weekly and monthly plans from top chefs.",
                icon = Icons.Default.RestaurantMenu,
                onClick = onNavigateToBrowse
            )
        }
    }
}

@Composable
fun HubCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
