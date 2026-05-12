package com.example.food.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.example.food.data.datastore.AppTheme

object ThemeManager {
    @Composable
    fun shouldUserDarkTheme(appTheme: AppTheme): Boolean {
        return when (appTheme) {
            AppTheme.DARK -> true
            AppTheme.LIGHT -> false
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }
    }
}
