package com.example.food.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

enum class AppTheme { DARK, LIGHT, SYSTEM }
enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    AMHARIC("አማርኛ", "am"),
    OROMO("Afaan Oromo", "om")
}

class SettingsRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_NOTIFICATIONS_ORDER = booleanPreferencesKey("notif_order")
        private val KEY_NOTIFICATIONS_PROMO = booleanPreferencesKey("notif_promo")
        private val KEY_NOTIFICATIONS_SYSTEM = booleanPreferencesKey("notif_system")
        private val KEY_NOTIFICATIONS_VENDOR = booleanPreferencesKey("notif_vendor")
        private val KEY_NOTIFICATIONS_CHEF = booleanPreferencesKey("notif_chef")
        private val KEY_NOTIFICATIONS_SUPPORT = booleanPreferencesKey("notif_support")
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }

    val theme: Flow<AppTheme> = context.settingsDataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            "LIGHT" -> AppTheme.LIGHT
            "SYSTEM" -> AppTheme.SYSTEM
            else -> AppTheme.DARK
        }
    }

    val notificationsOrder: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ORDER] ?: true
    }

    val notificationsPromo: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_PROMO] ?: true
    }

    val notificationsSystem: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_SYSTEM] ?: true
    }

    val notificationsVendor: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_VENDOR] ?: true
    }

    val notificationsChef: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_CHEF] ?: true
    }

    val notificationsSupport: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_SUPPORT] ?: true
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

    suspend fun setNotificationsOrder(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_ORDER] = enabled }
        syncToFirestore()
    }

    suspend fun setNotificationsPromo(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_PROMO] = enabled }
        syncToFirestore()
    }

    suspend fun setNotificationsSystem(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_SYSTEM] = enabled }
        syncToFirestore()
    }

    suspend fun setNotificationsVendor(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_VENDOR] = enabled }
        syncToFirestore()
    }

    suspend fun setNotificationsChef(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_CHEF] = enabled }
        syncToFirestore()
    }

    suspend fun setNotificationsSupport(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NOTIFICATIONS_SUPPORT] = enabled }
        syncToFirestore()
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[KEY_LANGUAGE] = language.code }
    }

    private suspend fun syncToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        val prefs = context.settingsDataStore.data.first()
        
        val notificationSettings = mapOf(
            "orderUpdates" to (prefs[KEY_NOTIFICATIONS_ORDER] ?: true),
            "promotions" to (prefs[KEY_NOTIFICATIONS_PROMO] ?: true),
            "systemUpdates" to (prefs[KEY_NOTIFICATIONS_SYSTEM] ?: true),
            "vendorUpdates" to (prefs[KEY_NOTIFICATIONS_VENDOR] ?: true),
            "chefBookings" to (prefs[KEY_NOTIFICATIONS_CHEF] ?: true),
            "supportTickets" to (prefs[KEY_NOTIFICATIONS_SUPPORT] ?: true)
        )

        try {
            firestore.collection("users").document(userId)
                .update("notificationSettings", notificationSettings)
                .await()
        } catch (e: Exception) {
            // If update fails (e.g. field doesn't exist), try set with merge
            firestore.collection("users").document(userId)
                .set(mapOf("notificationSettings" to notificationSettings), com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }
}
