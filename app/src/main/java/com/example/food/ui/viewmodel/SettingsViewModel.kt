package com.example.food.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.datastore.AppLanguage
import com.example.food.data.datastore.AppTheme
import com.example.food.data.datastore.SettingsRepository
import com.example.food.data.datastore.NotificationSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    val theme = repo.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.DARK)
    val notificationSettings = repo.notificationSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings())
    val language = repo.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    fun setTheme(theme: AppTheme) = viewModelScope.launch { repo.setTheme(theme) }
    
    fun updateNotificationSetting(userId: String?, category: String, enabled: Boolean) {
        viewModelScope.launch {
            repo.updateNotificationSetting(userId, category, enabled)
        }
    }
    
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { repo.setLanguage(lang) }
}
