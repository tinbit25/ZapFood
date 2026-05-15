package com.example.food.domain.manager

import com.example.food.core.util.Resource
import com.example.food.data.model.SmartTableSession
import com.example.food.data.repository.OrderRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * TableSessionManager
 * Manages the active smart table session for the user.
 */
class TableSessionManager(
    context: android.content.Context,
    private val orderRepository: OrderRepository = OrderRepository()
) {
    private val dataStore = com.example.food.data.datastore.TableSessionDataStore(context)
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.Job())
    private val _activeSession = MutableStateFlow<SmartTableSession?>(null)
    val activeSession: StateFlow<SmartTableSession?> = _activeSession.asStateFlow()

    init {
        // Load persisted session
        scope.launch {
            dataStore.getSession().collect { persistent ->
                if (persistent != null) {
                    _activeSession.value = SmartTableSession(
                        userId = persistent.userId,
                        vendorId = persistent.vendorId,
                        tableId = persistent.tableId,
                        tableNumber = persistent.tableNumber,
                        branchId = persistent.branchId
                    )
                }
            }
        }
    }

    fun startSession(userId: String, vendorId: String, tableId: String, tableNumber: String, branchId: String) {
        val session = SmartTableSession(
            userId = userId,
            vendorId = vendorId,
            tableId = tableId,
            tableNumber = tableNumber,
            branchId = branchId
        )
        _activeSession.value = session
        
        // Persist
        scope.launch {
            dataStore.saveSession(
                com.example.food.data.datastore.PersistentTableSession(
                    vendorId = vendorId,
                    tableId = tableId,
                    tableNumber = tableNumber,
                    branchId = branchId,
                    userId = userId
                )
            )
        }
    }

    fun endSession() {
        _activeSession.value = null
        scope.launch {
            dataStore.clearSession()
        }
    }
}
