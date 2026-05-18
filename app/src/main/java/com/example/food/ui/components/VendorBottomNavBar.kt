package com.example.food.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.food.ui.navigation.Screen

// Vendor Merchant OS Navigation — Kitchen-focused, NO customer discovery tabs.
sealed class VendorNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Dashboard : VendorNavItem("Dashboard", Icons.Default.Dashboard,     Screen.VendorDashboard.route)
    object Orders    : VendorNavItem("Orders",    Icons.Default.ListAlt,       Screen.VendorOrders.route)
    object Scanner   : VendorNavItem("QR Scan",   Icons.Default.QrCodeScanner, Screen.VendorPickupScan.route)
    object Analytics : VendorNavItem("Analytics", Icons.Default.Analytics,     Screen.VendorAnalytics.route)
    object Store     : VendorNavItem("Store",     Icons.Default.Store,          Screen.VendorStore.route)
}

@Composable
fun VendorBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        VendorNavItem.Dashboard,
        VendorNavItem.Orders,
        VendorNavItem.Scanner,
        VendorNavItem.Analytics,
        VendorNavItem.Store
    )
    val orange = Color(0xFFF16B24)
    NavigationBar(
        containerColor = Color(0xFF1A1A1A),
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon  = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { if (currentRoute != item.route) onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = orange,
                    selectedTextColor   = orange,
                    unselectedIconColor = Color(0xFF6B6B6B),
                    unselectedTextColor = Color(0xFF6B6B6B),
                    indicatorColor      = orange.copy(alpha = 0.15f)
                )
            )
        }
    }
}
