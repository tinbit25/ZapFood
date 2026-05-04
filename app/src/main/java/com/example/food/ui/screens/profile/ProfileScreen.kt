package com.example.food.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.screens.auth.AuthViewModel
import com.example.food.data.model.UserRole

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = user?.photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500&auto=format&fit=crop",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.displayName ?: "Kengfack Sydney",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = user?.email ?: "sydney@example.com",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFF16B24).copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = user?.role?.name ?: "CUSTOMER",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFF16B24),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Edit, title = "Edit Profile", onClick = onNavigateToEdit)
            
            // Common Items
            ProfileMenuItem(icon = Icons.Default.Receipt, title = "My Orders", onClick = onNavigateToOrders)
            ProfileMenuItem(icon = Icons.Default.LocationOn, title = "Delivery Addresses", onClick = onNavigateToAddresses)
            
            // Role-Specific Items
            if (user?.role == UserRole.VENDOR) {
                ProfileMenuItem(icon = Icons.Default.Store, title = "Business Dashboard", onClick = { /* Navigate to Dashboard */ })
                ProfileMenuItem(icon = Icons.Default.RestaurantMenu, title = "Manage Menu", onClick = { /* Navigate to Menu Management */ })
            }
            
            if (user?.role == UserRole.CUSTOMER) {
                ProfileMenuItem(icon = Icons.Default.Favorite, title = "Favorite Meals", onClick = { /* Navigate to Favorites */ })
                ProfileMenuItem(icon = Icons.Default.Stars, title = "Reward Points: ${user?.rewardPoints}", onClick = { /* Show points */ })
            }

            ProfileMenuItem(icon = Icons.Default.Settings, title = "Settings", onClick = onNavigateToSettings)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Log Out",
                isDestructive = true,
                onClick = {
                    authViewModel.logout()
                    onLogout()
                }
            )
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val color = if (isDestructive) Color(0xFFE57373) else Color.White
            val iconColor = if (isDestructive) Color(0xFFE57373) else Color(0xFFF16B24)
            
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier.weight(1f)
            )
            if (!isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
    }
}
