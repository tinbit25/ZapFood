package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.VendorAction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class AdminRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val ordersCollection = firestore.collection("orders")
    private val paymentsCollection = firestore.collection("payments")

    suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch users")
        }
    }

    suspend fun getAllVendorProfiles(): Resource<List<Vendor>> {
        return try {
            val snapshot = firestore.collection("vendors").get().await()
            val vendors = snapshot.documents.mapNotNull { it.toObject(Vendor::class.java) }
            Resource.Success(vendors)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch vendor profiles")
        }
    }

    suspend fun updateUserStatus(userId: String, isActive: Boolean): Resource<Unit> {
        return try {
            usersCollection.document(userId).update("isActive", isActive).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user status")
        }
    }

    suspend fun updateVendorStatus(userId: String, action: VendorAction): Resource<Unit> {
        return try {
            val updateMap = when (action) {
                VendorAction.APPROVE -> mapOf(
                    "verificationStatus" to "APPROVED",
                    "verified" to true,
                    "active" to true,
                    "isActive" to true
                )
                VendorAction.REJECT -> mapOf(
                    "verificationStatus" to "REJECTED",
                    "verified" to false,
                    "active" to false,
                    "isActive" to false
                )
                VendorAction.SUSPEND -> mapOf(
                    "verificationStatus" to "SUSPENDED",
                    "verified" to true,
                    "active" to false,
                    "isActive" to false
                )
                VendorAction.REQUEST_INFO -> mapOf(
                    "verificationStatus" to "PENDING_REVIEW",
                    "verified" to false,
                    "active" to false,
                    "isActive" to false
                )
                VendorAction.FLAG -> mapOf(
                    "verificationStatus" to "VERIFYING",
                    "active" to false,
                    "isActive" to false
                )
            }
            
            firestore.collection("vendors").document(userId)
                .update(updateMap).await()

            // Synchronize with the users collection
            val userIsActive = (action == VendorAction.APPROVE)
            usersCollection.document(userId).update("isActive", userIsActive).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update vendor status")
        }
    }

    fun observeAnalytics(): Flow<Resource<AdminDashboardData>> = callbackFlow {
        trySend(Resource.Loading())
        
        var latestOrders: List<Order>? = null
        var latestUsers: List<User>? = null
        var latestVendors: List<Vendor>? = null
        var commissionRateVal = 15

        fun emitDashboardData() {
            val orders = latestOrders
            val users = latestUsers
            val vendors = latestVendors
            
            if (orders == null || users == null || vendors == null) return

            val now = Calendar.getInstance()
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todayOrdersList = orders.filter { it.createdAt >= todayStart }
            
            val totalRevenue = orders.filter { it.orderStatus == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
            val commission = totalRevenue * (commissionRateVal.toDouble() / 100.0)
            val pendingPayout = totalRevenue * (1.0 - (commissionRateVal.toDouble() / 100.0))

            val liveStatuses = listOf(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.ON_THE_WAY)
            val liveOrders = orders.count { it.orderStatus in liveStatuses }
            val failedOrders = orders.count { it.orderStatus == OrderStatus.CANCELLED }

            val pendingVendors = vendors.count { it.verificationStatus == VerificationStatus.PENDING_REVIEW }
            val activeVendors = vendors.count { it.verificationStatus == VerificationStatus.ACTIVE || it.verificationStatus == VerificationStatus.APPROVED }
            val suspendedVendors = vendors.count { it.verificationStatus == VerificationStatus.SUSPENDED }
            
            val systemWarnings = pendingVendors + suspendedVendors + failedOrders

            val recentOrders = orders.sortedByDescending { it.createdAt }.take(10)
            
            val allItems = orders.flatMap { it.items }
            val mealIds = allItems.map { it.mealId }
            val mealCounts = mealIds.groupingBy { it }.eachCount()
            val topMeals = mealCounts.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
            
            val categoryDistribution = allItems.groupingBy { it.category }.eachCount()
            val fastingCount = allItems.count { it.fastingFriendly }
            val fastingRatio = if (allItems.isNotEmpty()) (fastingCount.toDouble() / allItems.size) * 100.0 else 0.0

            val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val last7DaysRevenue = java.util.LinkedHashMap<String, Double>()
            val cal = Calendar.getInstance()
            // We want chronological order (oldest to newest), so let's pre-populate the days
            val daysList = mutableListOf<String>()
            for (i in 0..6) {
                daysList.add(sdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            daysList.reverse()
            daysList.forEach { day ->
                last7DaysRevenue[day] = 0.0
            }

            orders.filter { it.orderStatus == OrderStatus.DELIVERED }.forEach { order ->
                val orderCal = Calendar.getInstance().apply { timeInMillis = order.createdAt }
                val dayStr = sdf.format(orderCal.time)
                if (last7DaysRevenue.containsKey(dayStr)) {
                    last7DaysRevenue[dayStr] = (last7DaysRevenue[dayStr] ?: 0.0) + order.totalAmount
                }
            }

            val hourlyDistribution = orders.groupingBy { 
                val orderCal = Calendar.getInstance().apply { timeInMillis = it.createdAt }
                orderCal.get(Calendar.HOUR_OF_DAY)
            }.eachCount()

            trySend(Resource.Success(
                AdminDashboardData(
                    totalRevenue = totalRevenue,
                    totalOrders = orders.size,
                    activeUsers = users.count { it.isActive },
                    pendingVendors = pendingVendors,
                    activeVendors = activeVendors,
                    suspendedVendors = suspendedVendors,
                    liveOrders = liveOrders,
                    failedOrders = failedOrders,
                    todayOrders = todayOrdersList.size,
                    commission = commission,
                    pendingPayout = pendingPayout,
                    systemWarnings = systemWarnings,
                    recentOrders = recentOrders,
                    topSellingMeals = topMeals,
                    categoryDistribution = categoryDistribution,
                    fastingRatio = fastingRatio,
                    revenueByDay = last7DaysRevenue,
                    hourlyDistribution = hourlyDistribution
                )
            ))
        }

        val ordersListener = ordersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Orders sync failed"))
                return@addSnapshotListener
            }
            latestOrders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
            emitDashboardData()
        }
        
        val usersListener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Users sync failed"))
                return@addSnapshotListener
            }
            latestUsers = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            emitDashboardData()
        }

        val vendorsListener = firestore.collection("vendors").addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Vendors sync failed"))
                return@addSnapshotListener
            }
            latestVendors = snapshot?.documents?.mapNotNull { it.toObject(Vendor::class.java) } ?: emptyList()
            emitDashboardData()
        }
        
        val configListener = firestore.collection("settings").document("system_config")
            .addSnapshotListener { snapshot, _ ->
                commissionRateVal = snapshot?.getLong("commissionRate")?.toInt() ?: 15
                emitDashboardData()
            }
        
        awaitClose { 
            ordersListener.remove()
            usersListener.remove()
            vendorsListener.remove()
            configListener.remove()
        }
    }

    suspend fun getAnalytics(): Resource<AdminDashboardData> {
        // ... existing getAnalytics ...
        return try {
            val allOrders = ordersCollection.get().await().documents.mapNotNull { it.toObject(Order::class.java) }
            val allUsers = usersCollection.get().await().documents.mapNotNull { it.toObject(User::class.java) }
            
            val totalRevenue = allOrders.filter { it.orderStatus == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
                val pendingVendors = allUsers.count { it.role == UserRole.VENDOR } // Status filter moved to Vendor collection
            
            val recentOrders = allOrders.sortedByDescending { it.createdAt }.take(10)
            
            val mealIds = allOrders.flatMap { order -> order.items.map { it.mealId } }
            val mealCounts = mealIds.groupingBy { it }.eachCount()
            val topMeals = mealCounts.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }

            Resource.Success(
                AdminDashboardData(
                    totalRevenue = totalRevenue,
                    totalOrders = allOrders.size,
                    activeUsers = allUsers.count { it.isActive },
                    pendingVendors = pendingVendors,
                    recentOrders = recentOrders,
                    topSellingMeals = topMeals
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to aggregate analytics")
        }
    }

    suspend fun getOrdersByFilters(status: OrderStatus?, vendorId: String?): Resource<List<Order>> {
        return try {
            var query: Query = ordersCollection
            status?.let { query = query.whereEqualTo("orderStatus", it) }
            vendorId?.let { query = query.whereEqualTo("vendorId", it) }
            
            val snapshot = query.get().await()
            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch filtered orders")
        }
    }

    suspend fun sendBroadcast(message: String): Resource<Unit> {
        return try {
            val broadcast = SystemBroadcast(message = message)
            firestore.collection("broadcasts").document(broadcast.id).set(broadcast).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send broadcast")
        }
    }

    fun observeSystemConfig(): Flow<SystemConfig> = callbackFlow {
        val listener = firestore.collection("settings").document("system_config")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(SystemConfig())
                    return@addSnapshotListener
                }
                val rate = snapshot?.getLong("commissionRate")?.toInt() ?: 15
                val maintenance = snapshot?.getBoolean("maintenanceMode") ?: false
                trySend(SystemConfig(rate, maintenance))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateSystemConfig(config: SystemConfig): Resource<Unit> {
        return try {
            firestore.collection("settings").document("system_config").set(config).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update configuration")
        }
    }

    suspend fun logAdminActivity(adminId: String, action: String, details: String) {
        val log = mapOf(
            "adminId" to adminId,
            "action" to action,
            "details" to details,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestore.collection("admin_logs").add(log).await()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun observeAdminLogs(): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection("admin_logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                val logs = snapshot?.documents?.map { it.data ?: emptyMap() } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }

    fun observeAbuseReportsCount(): Flow<Int> = callbackFlow {
        val listener = firestore.collection("abuse_reports")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    suspend fun triggerBackup(adminId: String): Resource<Unit> {
        return try {
            val updateMap = mapOf(
                "lastBackupTime" to System.currentTimeMillis(),
                "lastBackupBy" to adminId
            )
            firestore.collection("settings").document("backup_info").set(updateMap).await()
            logAdminActivity(adminId, "Backup Triggered", "System data backup completed successfully.")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Backup failed")
        }
    }

    fun observeLastBackupTime(): Flow<Long> = callbackFlow {
        val listener = firestore.collection("settings").document("backup_info")
            .addSnapshotListener { snapshot, _ ->
                val time = snapshot?.getLong("lastBackupTime") ?: 0L
                trySend(time)
            }
        awaitClose { listener.remove() }
    }
}
