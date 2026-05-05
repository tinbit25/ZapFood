package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MPCodeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val codesCollection = firestore.collection("mp_codes")
    private val usageCollection = firestore.collection("mp_code_usages")

    suspend fun saveCode(mpCode: MPCode): Resource<Unit> {
        return try {
            codesCollection.document(mpCode.code).set(mpCode).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save code")
        }
    }

    suspend fun getCode(code: String): MPCode? {
        return try {
            val doc = codesCollection.document(code).get().await()
            doc.toObject(MPCode::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun trackUsage(usage: MPCodeUsage): Resource<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val codeRef = codesCollection.document(usage.code)
                val currentCode = transaction.get(codeRef).toObject(MPCode::class.java)
                    ?: throw Exception("Code not found")
                
                transaction.update(codeRef, "usageCount", currentCode.usageCount + 1)
                transaction.set(usageCollection.document(usage.id), usage)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to track usage")
        }
    }
}

class RewardRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val rewardsCollection = firestore.collection("rewards")
    private val transactionsCollection = firestore.collection("reward_transactions")

    suspend fun getBalance(userId: String): Reward {
        return try {
            val doc = rewardsCollection.document(userId).get().await()
            doc.toObject(Reward::class.java) ?: Reward(userId = userId)
        } catch (e: Exception) {
            Reward(userId = userId)
        }
    }

    suspend fun processTransaction(transaction: RewardTransaction): Resource<Unit> {
        return try {
            firestore.runTransaction { firestoreTransaction ->
                val rewardRef = rewardsCollection.document(transaction.userId)
                val currentReward = firestoreTransaction.get(rewardRef).toObject(Reward::class.java)
                    ?: Reward(userId = transaction.userId)
                
                val newBalance = if (transaction.type == RewardTransactionType.EARN) {
                    currentReward.pointsBalance + transaction.points
                } else {
                    if (currentReward.pointsBalance < transaction.points) {
                        throw Exception("Insufficient points")
                    }
                    currentReward.pointsBalance - transaction.points
                }
                
                firestoreTransaction.set(rewardRef, currentReward.copy(
                    pointsBalance = newBalance,
                    updatedAt = System.currentTimeMillis()
                ))
                firestoreTransaction.set(transactionsCollection.document(transaction.id), transaction)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to process reward")
        }
    }
}
