package com.example.food.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

enum class AppTheme { DARK, LIGHT, SYSTEM }
enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    AMHARIC("አማርኛ", "am"),
    OROMO("Afaan Oromo", "om")
}

data class NotificationSettings(
    val orderUpdates: Boolean = true,
    val promotions: Boolean = true,
    val vendorUpdates: Boolean = true,
    val chefBookings: Boolean = true,
    val supportTickets: Boolean = true,
    val systemAnnouncements: Boolean = true
)

class SettingsRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_NOTIFICATIONS_ORDER = booleanPreferencesKey("notif_order")
        private val KEY_NOTIFICATIONS_PROMO = booleanPreferencesKey("notif_promo")
        private val KEY_NOTIFICATIONS_VENDOR = booleanPreferencesKey("notif_vendor")
        private val KEY_NOTIFICATIONS_CHEF = booleanPreferencesKey("notif_chef")
        private val KEY_NOTIFICATIONS_SUPPORT = booleanPreferencesKey("notif_support")
        private val KEY_NOTIFICATIONS_SYSTEM = booleanPreferencesKey("notif_system")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }

    val theme: Flow<AppTheme> = context.settingsDataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "SYSTEM" -> AppTheme.SYSTEM
            else -> AppTheme.DARK
        }
    }

    val notificationSettings: Flow<NotificationSettings> = context.settingsDataStore.data.map { prefs ->
        NotificationSettings(
            orderUpdates = prefs[KEY_NOTIFICATIONS_ORDER] ?: true,
            promotions = prefs[KEY_NOTIFICATIONS_PROMO] ?: true,
            vendorUpdates = prefs[KEY_NOTIFICATIONS_VENDOR] ?: true,
            chefBookings = prefs[KEY_NOTIFICATIONS_CHEF] ?: true,
            supportTickets = prefs[KEY_NOTIFICATIONS_SUPPORT] ?: true,
            systemAnnouncements = prefs[KEY_NOTIFICATIONS_SYSTEM] ?: true
        )
    }

    val language: Flow<AppLanguage> = context.settingsDataStore.data.map { prefs ->
        when (prefs[KEY_LANGUAGE]) {
            "am" -> AppLanguage.AMHARIC
            "om" -> AppLanguage.OROMO
            else -> AppLanguage.ENGLISH
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.settingsDataStore.edit { it[KEY_THEME] = theme.name }
    }

    suspend fun updateNotificationSetting(userId: String?, category: String, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            when (category) {
                "order" -> prefs[KEY_NOTIFICATIONS_ORDER] = enabled
                "promo" -> prefs[KEY_NOTIFICATIONS_PROMO] = enabled
                "vendor" -> prefs[KEY_NOTIFICATIONS_VENDOR] = enabled
                "chef" -> prefs[KEY_NOTIFICATIONS_CHEF] = enabled
                "support" -> prefs[KEY_NOTIFICATIONS_SUPPORT] = enabled
                "system" -> prefs[KEY_NOTIFICATIONS_SYSTEM] = enabled
            }
        }
        
        // Sync to Firestore if user is logged in
        if (userId != null) {
            try {
                val fieldName = when (category) {
                    "order" -> "notifOrder"
                    "promo" -> "notifPromo"
                    "vendor" -> "notifVendor"
                    "chef" -> "notifChef"
                    "support" -> "notifSupport"
                    "system" -> "notifSystem"
                    else -> null
                }
                if (fieldName != null) {
                    firestore.collection("users").document(userId)
                        .update("notificationPreferences.$fieldName", enabled)
                        .await()
                }
            } catch (e: Exception) {
                // Handle or log error
            }
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[KEY_LANGUAGE] = language.code }
    }
}
