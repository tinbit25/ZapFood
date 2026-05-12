package com.example.food.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.SettingsViewModel

@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val notifOrder by settingsViewModel.notificationsOrder.collectAsState()
    val notifPromo by settingsViewModel.notificationsPromo.collectAsState()
    val notifSystem by settingsViewModel.notificationsSystem.collectAsState()
    val notifVendor by settingsViewModel.notificationsVendor.collectAsState()
    val notifChef by settingsViewModel.notificationsChef.collectAsState()
    val notifSupport by settingsViewModel.notificationsSupport.collectAsState()

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "Notifications", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                "Customize how you want to be notified about your orders, promos, and more.",
                color = colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            NotificationSection("Orders & Bookings") {
                NotificationToggle(
                    icon = Icons.Default.ShoppingBag,
                    title = "Order Updates",
                    subtitle = "Real-time updates on your food delivery status",
                    checked = notifOrder,
                    onCheckedChange = { settingsViewModel.setNotificationsOrder(it) }
                )
                NotificationDivider()
                NotificationToggle(
                    icon = Icons.Default.Event,
                    title = "Chef Bookings",
                    subtitle = "Confirmations and reminders for private chef sessions",
                    checked = notifChef,
                    onCheckedChange = { settingsViewModel.setNotificationsChef(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            NotificationSection("Discovery & Offers") {
                NotificationToggle(
                    icon = Icons.Default.LocalOffer,
                    title = "Promotions",
                    subtitle = "Discounts, coupons, and seasonal offers",
                    checked = notifPromo,
                    onCheckedChange = { settingsViewModel.setNotificationsPromo(it) }
                )
                NotificationDivider()
                NotificationToggle(
                    icon = Icons.Default.Storefront,
                    title = "Vendor Updates",
                    subtitle = "New menus and posts from your favorite vendors",
                    checked = notifVendor,
                    onCheckedChange = { settingsViewModel.setNotificationsVendor(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            NotificationSection("Support & System") {
                NotificationToggle(
                    icon = Icons.Default.ConfirmationNumber,
                    title = "Support Tickets",
                    subtitle = "Replies and status updates for your support requests",
                    checked = notifSupport,
                    onCheckedChange = { settingsViewModel.setNotificationsSupport(it) }
                )
                NotificationDivider()
                NotificationToggle(
                    icon = Icons.Default.Campaign,
                    title = "System Updates",
                    subtitle = "Important app announcements and maintenance info",
                    checked = notifSystem,
                    onCheckedChange = { settingsViewModel.setNotificationsSystem(it) }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun NotificationSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
private fun NotificationToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colorScheme.primary,
                uncheckedThumbColor = colorScheme.onSurfaceVariant,
                uncheckedTrackColor = colorScheme.outline,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun NotificationDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outline
    )
}
