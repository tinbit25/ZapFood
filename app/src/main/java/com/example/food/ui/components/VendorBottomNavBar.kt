package com.example.food.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.food.ui.navigation.Screen

sealed class VendorNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Dashboard : VendorNavItem("Dashboard", Icons.Default.Dashboard, Screen.VendorDashboard.route)
    object Orders : VendorNavItem("Orders", Icons.Default.ReceiptLong, Screen.VendorOrders.route)
    object Menu : VendorNavItem("Menu", Icons.Default.Menu, Screen.VendorMenu.route)
    object Analytics : VendorNavItem("Analytics", Icons.Default.Assessment, Screen.VendorAnalytics.route)
    object Store : VendorNavItem("Store", Icons.Default.Storefront, Screen.VendorStore.route)
}

@Composable
fun VendorBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        VendorNavItem.Dashboard,
        VendorNavItem.Orders,
        VendorNavItem.Menu,
        VendorNavItem.Analytics,
        VendorNavItem.Store
    )

    NavigationBar(
        containerColor = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(item.title, style = androidx.compose.material3.MaterialTheme.typography.labelSmall) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color(0xFFF16B24),
                    selectedTextColor = androidx.compose.ui.graphics.Color(0xFFF16B24),
                    unselectedIconColor = androidx.compose.ui.graphics.Color(0xFF6B6B6B),
                    unselectedTextColor = androidx.compose.ui.graphics.Color(0xFF6B6B6B),
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF16B24).copy(alpha = 0.15f)
                )
            )
        }
    }
}
