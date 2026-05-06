package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun updateUserStatus(userId: String, isActive: Boolean): Resource<Unit> {
        return try {
            usersCollection.document(userId).update("isActive", isActive).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update user status")
        }
    }

    suspend fun updateVendorStatus(userId: String, status: VendorStatus): Resource<Unit> {
        return try {
            usersCollection.document(userId).update("vendorStatus", status).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update vendor status")
        }
    }

    suspend fun getAnalytics(): Resource<AdminDashboardData> {
        return try {
            // In a production app, we would use Firestore Aggregations or a Cloud Function
            // For now, we fetch recent data to build the dashboard
            val allOrders = ordersCollection.get().await().documents.mapNotNull { it.toObject(Order::class.java) }
            val allUsers = usersCollection.get().await().documents.mapNotNull { it.toObject(User::class.java) }
            
            val totalRevenue = allOrders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
            val pendingVendors = allUsers.count { it.role == UserRole.VENDOR && it.vendorStatus == VendorStatus.PENDING }
            
            val recentOrders = allOrders.sortedByDescending { it.createdAt }.take(10)
            
            // Basic aggregation for top meals (mocking meal names from IDs)
            // Basic aggregation for top meals (mocking meal names from IDs)
            // Extract meal IDs from order items
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
            status?.let { query = query.whereEqualTo("status", it) }
            vendorId?.let { query = query.whereEqualTo("vendorId", it) }
            
            val snapshot = query.get().await()
            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            Resource.Success(orders)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to fetch filtered orders")
        }
    }
}
