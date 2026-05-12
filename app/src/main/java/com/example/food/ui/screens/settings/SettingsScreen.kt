package com.example.food.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.food.data.datastore.AppLanguage
import com.example.food.data.datastore.AppTheme
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.SettingsViewModel
import com.example.food.ui.viewmodel.UserViewModel

private val Orange = Color(0xFFF16B24)
private val DarkBg = Color(0xFF0F0F0F)
private val CardBg = Color(0xFF1A1A1A)
private val DividerGray = Color(0xFF2A2A2A)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF9E9E9E)
private val RedDelete = Color(0xFFE53935)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel(),
    userViewModel: UserViewModel
) {
    val theme by settingsViewModel.theme.collectAsState()
    val notifOrder by settingsViewModel.notificationsOrder.collectAsState()
    val notifPromo by settingsViewModel.notificationsPromo.collectAsState()
    val notifSystem by settingsViewModel.notificationsSystem.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        TopNavBar(title = "Settings", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {

            // ─── APPEARANCE ───────────────────────────────────────────────
            SectionHeader("🎨  Appearance")
            Spacer(Modifier.height(12.dp))

            Text("Theme", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeChip(
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    selected = theme == AppTheme.DARK,
                    modifier = Modifier.weight(1f)
                ) { settingsViewModel.setTheme(AppTheme.DARK) }

                ThemeChip(
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    selected = theme == AppTheme.LIGHT,
                    modifier = Modifier.weight(1f)
                ) { settingsViewModel.setTheme(AppTheme.LIGHT) }

                ThemeChip(
                    label = "System",
                    icon = Icons.Default.SettingsBrightness,
                    selected = theme == AppTheme.SYSTEM,
                    modifier = Modifier.weight(1f)
                ) { settingsViewModel.setTheme(AppTheme.SYSTEM) }
            }

            Spacer(Modifier.height(24.dp))

            // ─── NOTIFICATIONS ────────────────────────────────────────────
            SectionHeader("🔔  Notifications")
            Spacer(Modifier.height(12.dp))

            SettingsCard {
                ToggleRow(
                    icon = Icons.Default.ShoppingBag,
                    title = "Order Updates",
                    subtitle = "Status changes, delivery alerts",
                    checked = notifOrder,
                    onCheckedChange = { settingsViewModel.setNotificationsOrder(it) }
                )
                SettingsDivider()
                ToggleRow(
                    icon = Icons.Default.LocalOffer,
                    title = "Promotions & Deals",
                    subtitle = "Special offers and discounts",
                    checked = notifPromo,
                    onCheckedChange = { settingsViewModel.setNotificationsPromo(it) }
                )
                SettingsDivider()
                ToggleRow(
                    icon = Icons.Default.Campaign,
                    title = "System Announcements",
                    subtitle = "App updates and important info",
                    checked = notifSystem,
                    onCheckedChange = { settingsViewModel.setNotificationsSystem(it) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ─── LANGUAGE ─────────────────────────────────────────────────
            SectionHeader("🌍  Language")
            Spacer(Modifier.height(12.dp))

            SettingsCard {
                ActionRow(
                    icon = Icons.Default.Language,
                    title = "App Language",
                    subtitle = language.displayName,
                    trailingIcon = Icons.Default.ChevronRight,
                    onClick = { showLanguagePicker = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            // ─── ACCOUNT ──────────────────────────────────────────────────
            SectionHeader("👤  Account")
            Spacer(Modifier.height(12.dp))

            SettingsCard {
                ActionRow(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out from your account",
                    trailingIcon = Icons.Default.ChevronRight,
                    onClick = { showLogoutDialog = true }
                )
                SettingsDivider()
                ActionRow(
                    icon = Icons.Default.DeleteForever,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account",
                    trailingIcon = Icons.Default.ChevronRight,
                    titleColor = RedDelete,
                    onClick = { showDeleteDialog = true }
                )
            }

            Spacer(Modifier.height(40.dp))

            // Version info
            Text(
                text = "ZapFood v1.0.0",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    // Language Picker Dialog
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            containerColor = CardBg,
            title = {
                Text("Select Language", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = lang == language
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Orange.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    settingsViewModel.setLanguage(lang)
                                    showLanguagePicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                color = if (isSelected) Orange else TextWhite,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Orange, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Logout",
            message = "Are you sure you want to sign out?",
            confirmLabel = "Logout",
            confirmColor = Orange,
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Account",
            message = "This action is permanent and cannot be undone. All your data will be lost.",
            confirmLabel = "Delete",
            confirmColor = RedDelete,
            onConfirm = {
                showDeleteDialog = false
                userViewModel.deleteAccount {
                    onLogout()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// ─── HELPER COMPOSABLES ───────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Orange,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = DividerGray
    )
}

@Composable
private fun ThemeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Orange else CardBg,
        animationSpec = tween(300),
        label = "theme_chip_bg"
    )
    val contentColor = if (selected) Color.White else TextGray

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, if (selected) Orange else DividerGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Orange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Orange,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = DividerGray
            )
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingIcon: ImageVector,
    titleColor: Color = TextWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(titleColor.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextGray, fontSize = 12.sp)
        }
        Icon(trailingIcon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = TextWhite, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextGray, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = confirmColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
