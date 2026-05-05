package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.data.repository.MPCodeRepository
import com.example.food.data.repository.MealPlanRepository
import com.example.food.data.repository.RewardRepository
import java.util.UUID

class MPCodeUseCase(
    private val codeRepository: MPCodeRepository = MPCodeRepository(),
    private val mealPlanRepository: MealPlanRepository = MealPlanRepository(),
    private val rewardRepository: RewardRepository = RewardRepository()
) {
    suspend fun generateCode(user: User, planId: String): Resource<String> {
        // 1. Fetch Plan
        val plan = mealPlanRepository.getMealPlanById(planId) ?: return Resource.Error("Plan not found")
        
        // 2. Security: Only owner can generate
        if (plan.ownerId != user.userId) return Resource.Error("Unauthorized")
        
        // 3. Check if already has code
        if (plan.mpcode.isNotEmpty()) return Resource.Success(plan.mpcode)
        
        // 4. Generate unique code
        val code = generateUniqueShortCode()
        val mpCode = MPCode(code = code, mealPlanId = planId, ownerId = user.userId)
        
        // 5. Save and link
        val result = codeRepository.saveCode(mpCode)
        if (result is Resource.Success) {
            mealPlanRepository.saveMealPlan(plan.copy(mpcode = code))
            return Resource.Success(code)
        }
        return Resource.Error("Failed to generate code")
    }

    suspend fun importPlan(user: User, code: String): Resource<MealPlan> {
        // 1. Fetch Code
        val mpCode = codeRepository.getCode(code) ?: return Resource.Error("Invalid code")
        if (!mpCode.isActive) return Resource.Error("Code is no longer active")
        
        // 2. Security: Cannot use own code
        if (mpCode.ownerId == user.userId) return Resource.Error("You cannot use your own code")
        
        // 3. Fetch original plan
        val originalPlan = mealPlanRepository.getMealPlanById(mpCode.mealPlanId) ?: return Resource.Error("Plan no longer exists")
        
        // 4. Clone for new user
        val clonedPlan = originalPlan.copy(
            id = UUID.randomUUID().toString(),
            ownerId = user.userId,
            sourceType = PlanSourceType.AI, // Or create SHARED source type
            mpcode = "" // Cloned plan doesn't have a code yet
        )
        
        // 5. Save cloned plan
        val saveResult = mealPlanRepository.saveMealPlan(clonedPlan)
        if (saveResult is Resource.Success<*>) {
            // 6. Track Usage and Reward Owner
            codeRepository.trackUsage(MPCodeUsage(code = code, usedByUserId = user.userId))
            rewardRepository.processTransaction(RewardTransaction(
                userId = mpCode.ownerId,
                points = 10,
                type = RewardTransactionType.EARN,
                source = RewardSource.MPCODE_USAGE
            ))
            return Resource.Success(clonedPlan)
        }
        return Resource.Error("Failed to import plan")
    }

    private fun generateUniqueShortCode(): String {
        return UUID.randomUUID().toString().substring(0, 6).uppercase()
    }
}
