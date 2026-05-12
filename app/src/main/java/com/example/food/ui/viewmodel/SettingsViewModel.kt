package com.example.food.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.data.datastore.AppLanguage
import com.example.food.data.datastore.AppTheme
import com.example.food.data.datastore.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    val theme = repo.theme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.DARK)
    val notificationsOrder = repo.notificationsOrder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsPromo = repo.notificationsPromo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsSystem = repo.notificationsSystem.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsVendor = repo.notificationsVendor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsChef = repo.notificationsChef.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsSupport = repo.notificationsSupport.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val language = repo.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    fun setTheme(theme: AppTheme) = viewModelScope.launch { repo.setTheme(theme) }
    fun setNotificationsOrder(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsOrder(enabled) }
    fun setNotificationsPromo(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsPromo(enabled) }
    fun setNotificationsSystem(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsSystem(enabled) }
    fun setNotificationsVendor(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsVendor(enabled) }
    fun setNotificationsChef(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsChef(enabled) }
    fun setNotificationsSupport(enabled: Boolean) = viewModelScope.launch { repo.setNotificationsSupport(enabled) }
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { repo.setLanguage(lang) }
}
