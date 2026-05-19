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
            val commission = totalRevenue * 0.15
            val pendingPayout = totalRevenue * 0.85

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
                    fastingRatio = fastingRatio
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
        
        awaitClose { 
            ordersListener.remove()
            usersListener.remove()
            vendorsListener.remove()
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
}
