package com.example.food.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.components.ProfileImage
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.screens.auth.AuthViewModel
import com.example.food.data.model.UserRole

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel,
    onLogout: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToVendorDashboard: () -> Unit,
    onNavigateToVendorMenu: () -> Unit,
    onNavigateToSupportTickets: () -> Unit,
    onNavigateToAdminSupport: () -> Unit,
    onNavigateToLinkPhone: () -> Unit,
    onNavigateToVendorRegistration: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val pointsBalance by rewardViewModel.pointsBalance.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(user) {
        user?.let { rewardViewModel.fetchBalance(it.userId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileImage(
                photoUrl = user?.photoUrl,
                displayName = user?.displayName,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = user?.displayName ?: "User Name",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
            Text(
                text = user?.email ?: "Email not available",
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = user?.role?.name ?: "CUSTOMER",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Edit, title = "Edit Profile", onClick = onNavigateToEdit)
            
            ProfileMenuItem(icon = Icons.Default.Receipt, title = "My Orders", onClick = onNavigateToOrders)
            ProfileMenuItem(icon = Icons.Default.LocationOn, title = "Delivery Addresses", onClick = onNavigateToAddresses)
            ProfileMenuItem(icon = Icons.Default.Support, title = "Help & Support", onClick = onNavigateToSupportTickets)
            
            if (user?.role == UserRole.ADMIN) {
                ProfileMenuItem(icon = Icons.Default.Security, title = "Admin Panel", onClick = onNavigateToAdmin)
                ProfileMenuItem(icon = Icons.Default.SupportAgent, title = "Support Dashboard", onClick = onNavigateToAdminSupport)
            }
            
            if (user?.role == UserRole.VENDOR) {
                ProfileMenuItem(icon = Icons.Default.Store, title = "Business Dashboard", onClick = onNavigateToVendorDashboard)
                ProfileMenuItem(icon = Icons.Default.RestaurantMenu, title = "Manage Menu", onClick = onNavigateToVendorMenu)
            }
            
            if (user?.role == UserRole.CUSTOMER) {
                ProfileMenuItem(icon = Icons.Default.Favorite, title = "Favorite Meals", onClick = { /* Navigate to Favorites */ })
                ProfileMenuItem(icon = Icons.Default.Stars, title = "Reward Points: $pointsBalance", onClick = { /* Show points details */ })
                
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    onClick = onNavigateToVendorRegistration,
                    modifier = Modifier.fillMaxWidth(),
                    color = colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null, tint = colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Become a Vendor", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                            Text("Sell your food on ZapFood", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            ProfileMenuItem(icon = Icons.Default.Settings, title = "Settings", onClick = onNavigateToSettings)
            
            if (user?.phoneNumber.isNullOrEmpty()) {
                ProfileMenuItem(
                    icon = Icons.Default.Phone,
                    title = "Link Phone Number",
                    onClick = onNavigateToLinkPhone
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
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
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val titleColor = if (isDestructive) colorScheme.error else colorScheme.onSurface
            val iconColor = if (isDestructive) colorScheme.error else colorScheme.primary
            
            Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            if (!isDestructive) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.5f))
    }
}
