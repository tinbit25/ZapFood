package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.AdminRepository
import kotlinx.coroutines.flow.Flow

class AdminUseCase(
    private val adminRepository: AdminRepository = AdminRepository()
) {
    // ... existing methods ...

    fun observeDashboardData(): Flow<Resource<AdminDashboardData>> {
        return adminRepository.observeAnalytics()
    }

    suspend fun getUsers(searchQuery: String = "", roleFilter: UserRole? = null): Resource<List<User>> {
        val result = adminRepository.getAllUsers()
        if (result is Resource.Success) {
            var filtered = result.data ?: emptyList()
            if (searchQuery.isNotBlank()) {
                filtered = filtered.filter { 
                    it.displayName?.contains(searchQuery, ignoreCase = true) == true || 
                    it.email.contains(searchQuery, ignoreCase = true) 
                }
            }
            if (roleFilter != null) {
                filtered = filtered.filter { it.role == roleFilter }
            }
            return Resource.Success(filtered)
        }
        return result
    }

    suspend fun getVendorApplications(): Resource<List<VendorApplication>> {
        val usersResult = getUsers(roleFilter = UserRole.VENDOR)
        val profilesResult = adminRepository.getAllVendorProfiles()
        
        if (usersResult is Resource.Success && profilesResult is Resource.Success) {
            val users = usersResult.data ?: emptyList()
            val profiles = profilesResult.data ?: emptyList()
            
            val applications = users.map { user ->
                VendorApplication(
                    user = user,
                    vendor = profiles.find { it.userId == user.userId }
                )
            }
            return Resource.Success(applications)
        }
        return Resource.Error(usersResult.message ?: profilesResult.message ?: "Failed to fetch vendor applications")
    }

    suspend fun toggleUserStatus(user: User): Resource<Unit> {
        return adminRepository.updateUserStatus(user.userId, !user.isActive)
    }

    suspend fun manageVendor(userId: String, action: VendorAction): Resource<Unit> {
        return adminRepository.updateVendorStatus(userId, action)
    }

    suspend fun getDashboardData(): Resource<AdminDashboardData> {
        return adminRepository.getAnalytics()
    }

    suspend fun sendBroadcast(message: String): Resource<Unit> {
        return adminRepository.sendBroadcast(message)
    }

    fun observeSystemConfig(): Flow<SystemConfig> {
        return adminRepository.observeSystemConfig()
    }

    suspend fun updateSystemConfig(config: SystemConfig): Resource<Unit> {
        return adminRepository.updateSystemConfig(config)
    }

    suspend fun logAdminActivity(adminId: String, action: String, details: String) {
        adminRepository.logAdminActivity(adminId, action, details)
    }

    fun observeAdminLogs(): Flow<List<Map<String, Any>>> {
        return adminRepository.observeAdminLogs()
    }

    fun observeAbuseReportsCount(): Flow<Int> {
        return adminRepository.observeAbuseReportsCount()
    }

    suspend fun triggerBackup(adminId: String): Resource<Unit> {
        return adminRepository.triggerBackup(adminId)
    }

    fun observeLastBackupTime(): Flow<Long> {
        return adminRepository.observeLastBackupTime()
    }
}

enum class VendorAction {
    APPROVE, REJECT, SUSPEND, REQUEST_INFO, FLAG
}
