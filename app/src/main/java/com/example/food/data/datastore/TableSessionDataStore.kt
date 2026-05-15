package com.example.food.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class PersistentTableSession(
    val vendorId: String,
    val tableId: String,
    val tableNumber: String,
    val branchId: String,
    val userId: String
)

class TableSessionDataStore(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val VENDOR_ID = stringPreferencesKey("table_vendor_id")
        val TABLE_ID = stringPreferencesKey("table_id")
        val TABLE_NUMBER = stringPreferencesKey("table_number")
        val BRANCH_ID = stringPreferencesKey("table_branch_id")
        val USER_ID = stringPreferencesKey("table_user_id")
        val IS_ACTIVE = booleanPreferencesKey("table_session_active")
    }

    suspend fun saveSession(session: PersistentTableSession) {
        dataStore.edit { prefs ->
            prefs[VENDOR_ID] = session.vendorId
            prefs[TABLE_ID] = session.tableId
            prefs[TABLE_NUMBER] = session.tableNumber
            prefs[BRANCH_ID] = session.branchId
            prefs[USER_ID] = session.userId
            prefs[IS_ACTIVE] = true
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs[IS_ACTIVE] = false
        }
    }

    fun getSession(): Flow<PersistentTableSession?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                val isActive = prefs[IS_ACTIVE] ?: false
                if (isActive) {
                    PersistentTableSession(
                        vendorId = prefs[VENDOR_ID] ?: "",
                        tableId = prefs[TABLE_ID] ?: "",
                        tableNumber = prefs[TABLE_NUMBER] ?: "",
                        branchId = prefs[BRANCH_ID] ?: "",
                        userId = prefs[USER_ID] ?: ""
                    )
                } else null
            }
    }
}
