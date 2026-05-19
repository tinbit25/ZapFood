package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.AdminUseCase
import com.example.food.domain.usecase.AnalyticsUseCase
import com.example.food.domain.usecase.VendorAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminUseCase: AdminUseCase = AdminUseCase(),
    private val analyticsUseCase: AnalyticsUseCase = AnalyticsUseCase()
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<Resource<AdminDashboardData>>(Resource.Loading())
    val dashboardState: StateFlow<Resource<AdminDashboardData>> = _dashboardState.asStateFlow()

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val usersState: StateFlow<Resource<List<User>>> = _usersState.asStateFlow()

    private val _vendorsState = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val vendorsState: StateFlow<Resource<List<User>>> = _vendorsState.asStateFlow()

    private val _vendorsApplicationsState = MutableStateFlow<Resource<List<VendorApplication>>>(Resource.Loading())
    val vendorsApplicationsState: StateFlow<Resource<List<VendorApplication>>> = _vendorsApplicationsState.asStateFlow()

    private val _systemHealth = MutableStateFlow(analyticsUseCase.getSystemHealth())
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()

    private val _systemConfigState = MutableStateFlow(SystemConfig())
    val systemConfigState: StateFlow<SystemConfig> = _systemConfigState.asStateFlow()

    private val _adminLogsState = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val adminLogsState: StateFlow<List<Map<String, Any>>> = _adminLogsState.asStateFlow()

    private val _abuseReportsCountState = MutableStateFlow(0)
    val abuseReportsCountState: StateFlow<Int> = _abuseReportsCountState.asStateFlow()

    private val _lastBackupTimeState = MutableStateFlow(0L)
    val lastBackupTimeState: StateFlow<Long> = _lastBackupTimeState.asStateFlow()

    init {
        observeDashboard()
        observeSystemConfig()
        observeLogsAndMetrics()
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            adminUseCase.observeDashboardData().collect {
                _dashboardState.value = it
                if (it is Resource.Success) {
                    _systemHealth.value = analyticsUseCase.getSystemHealth()
                }
            }
        }
    }

    fun refreshDashboard() {
        // Now handled by realtime observation
    }

    fun fetchUsers(query: String = "", role: UserRole? = null) {
        viewModelScope.launch {
            if (role == UserRole.VENDOR) {
                _vendorsState.value = Resource.Loading()
                _vendorsState.value = adminUseCase.getUsers(query, role)
                
                _vendorsApplicationsState.value = Resource.Loading()
                _vendorsApplicationsState.value = adminUseCase.getVendorApplications()
            } else {
                _usersState.value = Resource.Loading()
                _usersState.value = adminUseCase.getUsers(query, role)
            }
        }
    }

    fun toggleUserStatus(user: User) {
        viewModelScope.launch {
            val result = adminUseCase.toggleUserStatus(user)
            if (result is Resource.Success<*>) {
                fetchUsers() // Refresh list
            }
        }
    }

    fun updateVendorStatus(userId: String, action: VendorAction) {
        viewModelScope.launch {
            val result = adminUseCase.manageVendor(userId, action)
            if (result is Resource.Success<*>) {
                fetchUsers(role = UserRole.VENDOR) // Refresh vendor list
                refreshDashboard()
            }
        }
    }

    fun sendBroadcast(message: String, onResult: (Resource<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = adminUseCase.sendBroadcast(message)
            onResult(result)
        }
    }

    private fun observeSystemConfig() {
        viewModelScope.launch {
            adminUseCase.observeSystemConfig().collect {
                _systemConfigState.value = it
            }
        }
    }

    fun updateSystemConfig(config: SystemConfig) {
        viewModelScope.launch {
            adminUseCase.updateSystemConfig(config)
        }
    }

    private fun observeLogsAndMetrics() {
        viewModelScope.launch {
            adminUseCase.observeAdminLogs().collect {
                _adminLogsState.value = it
            }
        }
        viewModelScope.launch {
            adminUseCase.observeAbuseReportsCount().collect {
                _abuseReportsCountState.value = it
            }
        }
        viewModelScope.launch {
            adminUseCase.observeLastBackupTime().collect {
                _lastBackupTimeState.value = it
            }
        }
    }

    fun triggerBackup(adminId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            adminUseCase.triggerBackup(adminId)
            onComplete()
        }
    }

    fun logAdminActivity(adminId: String, action: String, details: String) {
        viewModelScope.launch {
            adminUseCase.logAdminActivity(adminId, action, details)
        }
    }
}
