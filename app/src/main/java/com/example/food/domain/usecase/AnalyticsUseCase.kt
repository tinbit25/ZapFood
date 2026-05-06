package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.AdminRepository
import java.util.Calendar

class AnalyticsUseCase(
    private val adminRepository: AdminRepository = AdminRepository()
) {
    suspend fun generateReport(type: ReportType): Resource<AdminReport> {
        val dashboardResult = adminRepository.getAnalytics()
        if (dashboardResult is Resource.Error) return Resource.Error(dashboardResult.message!!)
        
        val data = dashboardResult.data!!
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        val startDate = when (type) {
            ReportType.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
            ReportType.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            ReportType.MONTHLY -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
        }

        return Resource.Success(
            AdminReport(
                type = type,
                startDate = startDate,
                endDate = now,
                totalRevenue = data.totalRevenue,
                orderCount = data.totalOrders,
                newUserCount = data.activeUsers // Placeholder for new users in range
            )
        )
    }

    fun getSystemHealth(): SystemHealth {
        // Mocking system health for now
        return SystemHealth(
            apiResponseTime = (100..500).random().toLong(),
            errorRate = (0..200).random() / 100.0,
            failedPaymentsCount = (0..5).random()
        )
    }
}
