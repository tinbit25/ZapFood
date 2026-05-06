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

    suspend fun seedMeals(): Resource<Unit> {
        val seedList = listOf(
            Meal(name = "Classic Cheeseburger", price = 8.99, category = "Main Course", calories = 650, vendorName = "Burger King", imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500"),
            Meal(name = "Grilled Chicken Salad", price = 6.50, category = "Salad", calories = 350, vendorName = "Healthy Bites", imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"),
            Meal(name = "Mushroom Risotto", price = 12.00, category = "Main Course", calories = 550, vendorName = "Italiano", imageUrl = "https://images.unsplash.com/photo-1476124369491-e7addf5db371?w=500"),
            Meal(name = "Avocado Toast", price = 5.99, category = "Breakfast", calories = 300, vendorName = "Coffee House", imageUrl = "https://images.unsplash.com/photo-1525351484163-7529414344d8?w=500")
        )
        return try {
            seedList.forEach { meal ->
                mealsCollection.document(meal.id).set(meal).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Seeding failed")
        }
    }
}
