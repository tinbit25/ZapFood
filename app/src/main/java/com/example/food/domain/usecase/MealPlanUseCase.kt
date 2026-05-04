package com.example.food.domain.usecase

import com.example.food.data.model.MealPlan
import com.example.food.data.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MealPlanUseCase(
    private val mealRepository: MealRepository = MealRepository()
) {
    fun getMealPlans(): Flow<List<MealPlan>> = mealRepository.getMealPlans()

    fun getMealPlanById(id: String): MealPlan? = mealRepository.getMealPlanById(id)

    fun generateMPCode(plan: MealPlan): String {
        return if (plan.mpcode.isNotEmpty()) plan.mpcode 
        else "MP-${plan.id.takeLast(4)}-${(1000..9999).random()}"
    }

    fun clonePlanForUser(originalPlan: MealPlan, userId: String): MealPlan {
        return originalPlan.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "Custom ${originalPlan.name}",
            ownerId = userId,
            mpcode = ""
        )
    }
}
