package com.example.food.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.food.ui.navigation.Screen

@Composable
fun AdminBottomNavBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        Screen.AdminDashboard to "Dashboard",
        Screen.AdminVendorManagement to "Vendors",
        Screen.AdminOrderMonitoring to "Orders",
        Screen.AdminAnalytics to "Analytics",
        Screen.AdminControlCenter to "Control"
    )

    NavigationBar(
        containerColor = Color(0xFF0A0A0A), // Extremely dark for admin
        contentColor = Color.White
    ) {
        items.forEach { (screen, label) ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onNavigate(screen.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.AdminDashboard -> Icons.Default.Dashboard
                            Screen.AdminVendorManagement -> Icons.Default.Storefront
                            Screen.AdminOrderMonitoring -> Icons.Default.ListAlt
                            Screen.AdminAnalytics -> Icons.Default.BarChart
                            Screen.AdminControlCenter -> Icons.Default.Security
                            else -> Icons.Default.Circle
                        },
                        contentDescription = label
                    )
                },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF16B24),
                    selectedTextColor = Color(0xFFF16B24),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFF16B24).copy(alpha = 0.1f)
                )
            )
        }
    }
}
