package com.example.food.data.model

import java.util.Date

data class AdminDashboardData(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val activeUsers: Int = 0,
    val pendingVendors: Int = 0,
    val recentOrders: List<Order> = emptyList(),
    val revenueByDay: Map<String, Double> = emptyMap(), // Date string to revenue
    val topSellingMeals: List<Pair<String, Int>> = emptyList() // Meal name to count
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
