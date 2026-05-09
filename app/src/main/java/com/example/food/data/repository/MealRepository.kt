package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MealRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val mealsCollection = firestore.collection("meals")

    suspend fun saveMeal(meal: Meal): Resource<Unit> {
        return try {
            mealsCollection.document(meal.id).set(meal).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save meal")
        }
    }

    suspend fun getMealById(id: String): Meal? {
        return try {
            val doc = mealsCollection.document(id).get().await()
            doc.toObject(Meal::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getFilteredMeals(filters: MealFilters): Flow<Resource<List<Meal>>> = callbackFlow {
        trySend(Resource.Loading())

        var query: Query = mealsCollection

        filters.category?.let {
            query = query.whereEqualTo("category", it)
        }
        filters.vendorId?.let {
            query = query.whereEqualTo("vendorId", it)
        }
        filters.minCalories?.let {
            query = query.whereGreaterThanOrEqualTo("calories", it)
        }
        filters.maxCalories?.let {
            query = query.whereLessThanOrEqualTo("calories", it)
        }
        
        // Note: Querying by name prefix for search
        filters.query?.let {
            if (it.isNotEmpty()) {
                query = query.whereGreaterThanOrEqualTo("name", it)
                    .whereLessThanOrEqualTo("name", it + "\uf8ff")
            }
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                return@addSnapshotListener
            }

            val meals = snapshot?.documents?.mapNotNull { it.toObject(Meal::class.java) } ?: emptyList()
            trySend(Resource.Success(meals))
        }

        awaitClose { listener.remove() }
    }

    suspend fun deleteMeal(id: String): Resource<Unit> {
        return try {
            mealsCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete meal")
        }
    }

    suspend fun seedMeals(vendorIds: List<String>): Resource<Unit> {
        if (vendorIds.isEmpty()) return Resource.Error("No vendors found to assign meals to")
        
        val seedData = listOf(
            Triple("Classic Cheeseburger", 8.99, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800"),
            Triple("Crispy Chicken Sandwich", 7.50, "https://images.unsplash.com/photo-1626700051175-656868edfab9?w=800"),
            Triple("Vegetarian Garden Salad", 6.00, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800"),
            Triple("Spicy Tuna Roll", 12.00, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800"),
            Triple("Margarita Pizza", 15.00, "https://images.unsplash.com/photo-1574071318508-1cdbad80ad50?w=800"),
            Triple("Berry Smoothie Bowl", 9.50, "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=800")
        )

        return try {
            seedData.forEachIndexed { index, data ->
                val vendorId = vendorIds[index % vendorIds.size]
                val meal = Meal(
                    id = java.util.UUID.randomUUID().toString(),
                    name = data.first,
                    price = data.second,
                    imageUrl = data.third,
                    vendorId = vendorId,
                    vendorName = "Demo Vendor ${index + 1}",
                    category = if (index % 2 == 0) "Main Course" else "Healthy",
                    calories = 300 + (index * 50),
                    isAvailable = true
                )
                mealsCollection.document(meal.id).set(meal).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Seeding failed")
        }
    }
    suspend fun seedMealsForVendor(vendorId: String, vendorName: String): Resource<Unit> {
        val seedData = listOf(
            Triple("Classic Injera Combo", 8.99, "https://images.unsplash.com/photo-1541014741259-df549af00c67?w=800"),
            Triple("Spicy Shiro", 7.50, "https://images.unsplash.com/photo-1589647363585-f4a7d3877b10?w=800"),
            Triple("Kitfo Special", 12.00, "https://images.unsplash.com/photo-1606787366850-de6330128bfc?w=800")
        )

        return try {
            seedData.forEachIndexed { index, data ->
                val meal = Meal(
                    id = java.util.UUID.randomUUID().toString(),
                    name = data.first,
                    price = data.second,
                    imageUrl = data.third,
                    vendorId = vendorId,
                    vendorName = vendorName,
                    category = "Ethiopian",
                    calories = 400 + (index * 100),
                    isAvailable = true
                )
                mealsCollection.document(meal.id).set(meal).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Seeding failed")
        }
    }
}
