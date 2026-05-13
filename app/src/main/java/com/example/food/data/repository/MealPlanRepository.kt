package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.Day
import com.example.food.data.model.MealPlan
import com.example.food.data.model.PlanSourceType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MealPlanRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val plansCollection = firestore.collection("meal_plans")

    suspend fun saveMealPlan(plan: MealPlan): Resource<Unit> {
        return try {
            plansCollection.document(plan.id).set(plan).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save meal plan")
        }
    }

    suspend fun getMealPlanById(id: String): MealPlan? {
        return try {
            val doc = plansCollection.document(id).get().await()
            doc.toObject(MealPlan::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getMealPlansForUser(userId: String): Flow<Resource<List<MealPlan>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = plansCollection
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }

                val plans = snapshot?.documents?.mapNotNull { it.toObject(MealPlan::class.java) } ?: emptyList()
                trySend(Resource.Success(plans))
            }

        awaitClose { listener.remove() }
    }

    fun getVendorPlans(): Flow<Resource<List<MealPlan>>> = callbackFlow {
        trySend(Resource.Loading())

        val listener = plansCollection
            .whereEqualTo("sourceType", "VENDOR")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Query failed"))
                    return@addSnapshotListener
                }

                val plans = snapshot?.documents?.mapNotNull { it.toObject(MealPlan::class.java) } ?: emptyList()
                trySend(Resource.Success(plans))
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteMealPlan(id: String): Resource<Unit> {
        return try {
            plansCollection.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete meal plan")
        }
    }

    suspend fun seedPlans(vendorIds: List<String>, mealIds: List<String>): Resource<Unit> {
        if (vendorIds.isEmpty() || mealIds.isEmpty()) return Resource.Error("Insufficient data to seed plans")
        
        return try {
            vendorIds.forEachIndexed { vIndex, vendorId ->
                val plan = MealPlan(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Premium ${if (vIndex % 2 == 0) "Keto" else "Healthy"} Plan ${vIndex + 1}",
                    description = "A professionally curated meal plan by our experts.",
                    ownerId = vendorId,
                    vendorId = vendorId,
                    businessName = "Demo Vendor ${vIndex + 1}",
                    imageUrl = if (vIndex % 2 == 0) "https://images.unsplash.com/photo-1547573854-74d2a71d0827?w=800" else "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
                    price = 45.0 + vIndex,
                    meals = Day.entries.associateWith { day ->
                        listOf(mealIds[vIndex % mealIds.size], mealIds[(vIndex + 1) % mealIds.size])
                    },
                    sourceType = PlanSourceType.VENDOR,
                    isPublic = true
                )
                plansCollection.document(plan.id).set(plan).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Seeding plans failed")
        }
    }
}
