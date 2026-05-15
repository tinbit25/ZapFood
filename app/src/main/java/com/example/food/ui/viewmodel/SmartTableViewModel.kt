package com.example.food.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.OrderRepository
import com.example.food.data.repository.VendorRepository
import com.example.food.domain.manager.TableSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SmartTableUiState(
    val session: SmartTableSession? = null,
    val activeBooking: Order? = null,
    val vendor: Vendor? = null,
    val runningTab: List<Order> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSettling: Boolean = false
)

class SmartTableViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = TableSessionManager(application)
    private val orderRepository = OrderRepository()
    private val vendorRepository = VendorRepository()

    private val _uiState = MutableStateFlow(SmartTableUiState())
    val uiState: StateFlow<SmartTableUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.activeSession.collect { session ->
                _uiState.value = _uiState.value.copy(session = session)
                if (session != null) {
                    loadVendorInfo(session.vendorId)
                    loadRunningTab(session.userId, session.vendorId)
                }
            }
        }
    }

    fun checkForActiveBooking(userId: String) {
        viewModelScope.launch {
            val booking = orderRepository.getActiveDineInBooking(userId)
            _uiState.value = _uiState.value.copy(activeBooking = booking)
        }
    }

    fun parseQRCode(json: String, userId: String) {
        viewModelScope.launch {
            try {
                // In a real app, use a proper JSON parser like Gson or Kotlin Serialization
                // { "vendorId": "ETH_XYZ_01", "tableId": "T-05", "tableNumber": "5", "branchId": "DB_BRANCH_A" }
                val vendorId = json.substringAfter("\"vendorId\":\"").substringBefore("\"")
                val tableId = json.substringAfter("\"tableId\":\"").substringBefore("\"")
                val tableNumber = json.substringAfter("\"tableNumber\":\"").substringBefore("\"")
                val branchId = json.substringAfter("\"branchId\":\"").substringBefore("\"")

                sessionManager.startSession(userId, vendorId, tableId, tableNumber, branchId)
                
                // If there's an active booking, check-in
                val activeBooking = _uiState.value.activeBooking
                if (activeBooking != null && activeBooking.vendorId == vendorId) {
                    orderRepository.checkInToTable(activeBooking.orderId, tableNumber)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Invalid QR Code format")
            }
        }
    }

    private fun loadVendorInfo(vendorId: String) {
        viewModelScope.launch {
            val vendor = vendorRepository.getVendorById(vendorId)
            _uiState.value = _uiState.value.copy(vendor = vendor)
        }
    }

    private fun loadRunningTab(userId: String, vendorId: String) {
        viewModelScope.launch {
            orderRepository.getOrdersForUser(userId).collect { resource ->
                if (resource is Resource.Success) {
                    val activeOrders = resource.data?.filter { 
                        it.vendorId == vendorId && 
                        it.orderStatus != OrderStatus.DELIVERED && 
                        it.orderStatus != OrderStatus.CANCELLED 
                    } ?: emptyList()
                    
                    val total = activeOrders.sumOf { it.totalAmount }
                    _uiState.value = _uiState.value.copy(
                        runningTab = activeOrders,
                        totalAmount = total
                    )
                }
            }
        }
    }

    fun settleBill(onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val ordersToClose = _uiState.value.runningTab
            
            _uiState.value = _uiState.value.copy(isSettling = true)
            
            // In Arrive & Eat, closing the table is the final step
            // We loop through orders and mark them delivered
            var success = true
            for (order in ordersToClose) {
                val result = orderRepository.closeDineInTable(order.orderId)
                if (result is Resource.Error) success = false
            }
            
            _uiState.value = _uiState.value.copy(isSettling = false)
            
            if (success) {
                sessionManager.endSession()
                onResult(Resource.Success(Unit))
            } else {
                onResult(Resource.Error("Failed to settle all items"))
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
