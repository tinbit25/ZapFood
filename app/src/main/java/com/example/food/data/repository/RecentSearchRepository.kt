package com.example.food.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "recent_searches")

class RecentSearchRepository(private val context: Context) {
    private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches_list")

    val recentSearches: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val searchesString = preferences[RECENT_SEARCHES_KEY] ?: ""
            if (searchesString.isEmpty()) emptyList()
            else searchesString.split(",").filter { it.isNotBlank() }
        }

    suspend fun addSearch(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentSearches = (preferences[RECENT_SEARCHES_KEY] ?: "")
                .split(",")
                .filter { it.isNotBlank() && it != query }
                .toMutableList()
            
            // Add to front
            currentSearches.add(0, query)
            
            // Keep only top 10
            val updatedList = currentSearches.take(10).joinToString(",")
            preferences[RECENT_SEARCHES_KEY] = updatedList
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES_KEY)
        }
    }
}
