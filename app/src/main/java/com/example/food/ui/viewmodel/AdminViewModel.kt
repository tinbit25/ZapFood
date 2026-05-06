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

    private val _systemHealth = MutableStateFlow(analyticsUseCase.getSystemHealth())
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardState.value = Resource.Loading()
            _dashboardState.value = adminUseCase.getDashboardData()
            _systemHealth.value = analyticsUseCase.getSystemHealth()
        }
    }

    fun fetchUsers(query: String = "", role: UserRole? = null) {
        viewModelScope.launch {
            _usersState.value = Resource.Loading()
            _usersState.value = adminUseCase.getUsers(query, role)
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
}
