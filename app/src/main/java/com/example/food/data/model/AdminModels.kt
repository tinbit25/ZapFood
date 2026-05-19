package com.example.food.data.model

import java.util.Date

data class AdminDashboardData(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val activeUsers: Int = 0,
    val pendingVendors: Int = 0,
    val activeVendors: Int = 0,
    val suspendedVendors: Int = 0,
    val liveOrders: Int = 0,
    val failedOrders: Int = 0,
    val todayOrders: Int = 0,
    val commission: Double = 0.0,
    val pendingPayout: Double = 0.0,
    val systemWarnings: Int = 0,
    val recentOrders: List<Order> = emptyList(),
    val revenueByDay: Map<String, Double> = emptyMap(), // Date string to revenue
    val topSellingMeals: List<Pair<String, Int>> = emptyList(), // Meal name to count
    val categoryDistribution: Map<String, Int> = emptyMap(),
    val fastingRatio: Double = 0.0, // Percentage of fasting friendly meals ordered
    val hourlyDistribution: Map<Int, Int> = emptyMap()
)

data class SystemHealth(
    val apiResponseTime: Long = 0, // ms
    val errorRate: Double = 0.0, // percentage
    val failedPaymentsCount: Int = 0,
    val lastChecked: Long = System.currentTimeMillis()
)

data class AdminReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ReportType,
    val startDate: Long,
    val endDate: Long,
    val totalRevenue: Double,
    val orderCount: Int,
    val newUserCount: Int,
    val generatedAt: Long = System.currentTimeMillis()
)

enum class ReportType {
    DAILY, WEEKLY, MONTHLY
}

data class SystemBroadcast(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val senderId: String = "admin",
    val active: Boolean = true
)

data class SystemConfig(
    val commissionRate: Int = 15,
    val maintenanceMode: Boolean = false
)
