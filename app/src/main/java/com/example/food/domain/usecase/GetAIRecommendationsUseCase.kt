package com.example.food.domain.usecase

import com.example.food.data.repository.RecommendationRepository
import com.example.food.domain.model.AIRecommendationResponse

class GetAIRecommendationsUseCase(
    private val repository: RecommendationRepository = RecommendationRepository()
) {
    suspend operator fun invoke(userId: String): Result<AIRecommendationResponse> {
        return repository.getAIRecommendations(userId)
    }
}
