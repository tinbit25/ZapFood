package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.RewardRepository

class RewardUseCase(
    private val rewardRepository: RewardRepository = RewardRepository()
) {
    suspend fun getBalance(userId: String): Resource<Int> {
        val reward = rewardRepository.getBalance(userId)
        return Resource.Success(reward.pointsBalance)
    }

    suspend fun calculateDiscount(pointsToRedeem: Int): Double {
        // Rule: 10 points = 100 RWF discount
        return (pointsToRedeem / 10) * 100.0
    }

    suspend fun redeemPoints(userId: String, points: Int): Resource<Unit> {
        if (points <= 0) return Resource.Error("Invalid points")
        
        return rewardRepository.processTransaction(RewardTransaction(
            userId = userId,
            points = points,
            type = RewardTransactionType.REDEEM,
            source = RewardSource.ORDER
        ))
    }
}
