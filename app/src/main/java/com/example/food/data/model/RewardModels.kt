package com.example.food.data.model

import java.util.UUID

data class MPCode(
    val code: String = "",
    val mealPlanId: String = "",
    val ownerId: String = "",
    val usageCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class MPCodeUsage(
    val id: String = UUID.randomUUID().toString(),
    val code: String = "",
    val usedByUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Reward(
    val userId: String = "",
    val pointsBalance: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RewardTransactionType {
    EARN, REDEEM
}

enum class RewardSource {
    MPCODE_USAGE, ORDER, BONUS
}

data class RewardTransaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val points: Int = 0,
    val type: RewardTransactionType = RewardTransactionType.EARN,
    val source: RewardSource = RewardSource.ORDER,
    val createdAt: Long = System.currentTimeMillis()
)
