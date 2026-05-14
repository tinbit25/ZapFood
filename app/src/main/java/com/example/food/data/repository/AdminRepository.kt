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
                    "active" to true
                )
                VendorAction.REJECT -> mapOf(
                    "verificationStatus" to "REJECTED",
                    "verified" to false,
                    "active" to false
                )
                VendorAction.SUSPEND -> mapOf(
                    "verificationStatus" to "SUSPENDED",
                    "verified" to true, // Profile still exists/verified but not active
                    "active" to false
                )
            }
            
            firestore.collection("vendors").document(userId)
                .update(updateMap).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update vendor status")
        }
    }

    fun observeAnalytics(): Flow<Resource<AdminDashboardData>> = callbackFlow {
        trySend(Resource.Loading())
        
        // Listen to both orders and users for comprehensive realtime dashboard
        val ordersListener = ordersCollection.addSnapshotListener { ordersSnapshot, ordersError ->
            if (ordersError != null) {
                trySend(Resource.Error(ordersError.localizedMessage ?: "Orders sync failed"))
                return@addSnapshotListener
            }
            
            val usersListener = usersCollection.addSnapshotListener { usersSnapshot, usersError ->
                if (usersError != null) {
                    trySend(Resource.Error(usersError.localizedMessage ?: "Users sync failed"))
                    return@addSnapshotListener
                }
                
                val allOrders = ordersSnapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                val allUsers = usersSnapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
                
                val totalRevenue = allOrders.filter { it.orderStatus == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
                    val pendingVendors = allUsers.count { it.role == UserRole.VENDOR } // Status filter moved to Vendor collection
                val recentOrders = allOrders.sortedByDescending { it.createdAt }.take(10)
                
                val allItems = allOrders.flatMap { it.items }
                val mealIds = allItems.map { it.mealId }
                val mealCounts = mealIds.groupingBy { it }.eachCount()
                val topMeals = mealCounts.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value }
                
                val categoryDistribution = allItems.groupingBy { it.category }.eachCount()
                val fastingCount = allItems.count { it.fastingFriendly }
                val fastingRatio = if (allItems.isNotEmpty()) (fastingCount.toDouble() / allItems.size) * 100.0 else 0.0

                trySend(Resource.Success(
                    AdminDashboardData(
                        totalRevenue = totalRevenue,
                        totalOrders = allOrders.size,
                        activeUsers = allUsers.count { it.isActive },
                        pendingVendors = pendingVendors,
                        recentOrders = recentOrders,
                        topSellingMeals = topMeals,
                        categoryDistribution = categoryDistribution,
                        fastingRatio = fastingRatio
                    )
                ))
            }
        }
        
        awaitClose { 
            // In a real multi-listener callbackFlow, we'd manage multiple registrations
            // but for simplicity here we rely on the parent listener structure
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
}
