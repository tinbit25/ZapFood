package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.AdminRepository

class AdminUseCase(
    private val adminRepository: AdminRepository = AdminRepository()
) {
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

    suspend fun toggleUserStatus(user: User): Resource<Unit> {
        return adminRepository.updateUserStatus(user.userId, !user.isActive)
    }

    suspend fun manageVendor(userId: String, action: VendorAction): Resource<Unit> {
        val status = when (action) {
            VendorAction.APPROVE -> VendorStatus.APPROVED
            VendorAction.REJECT -> VendorStatus.REJECTED
            VendorAction.SUSPEND -> VendorStatus.SUSPENDED
        }
        return adminRepository.updateVendorStatus(userId, status)
    }

    suspend fun getDashboardData(): Resource<AdminDashboardData> {
        return adminRepository.getAnalytics()
    }
}

enum class VendorAction {
    APPROVE, REJECT, SUSPEND
}
