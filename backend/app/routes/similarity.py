import time
from fastapi import APIRouter, HTTPException, Depends
from app.models.recommendation import SimilarityRequest, RecommendationResponse
from ai.similarity_engine import MLSimilarityEngine
from app.repositories.meal_repository import MealRepository

router = APIRouter(prefix="/similarity", tags=["Similarity ML"])
ml_engine = MLSimilarityEngine()
meal_repo = MealRepository()

@router.get("/meals/{meal_id}/similar", response_model=RecommendationResponse)
async def get_similar_meals_get(meal_id: str):
    """
    Stateful GET Endpoint: Fetches candidate meals from the Database via MealRepository.
    Returns similar meals based on ML computation.
    """
    start_time = time.time()
    try:
        # Fetch all available meals from database
        candidate_meals = meal_repo.get_available_meals()
        
        if not candidate_meals:
            return RecommendationResponse(recommendations=[], processing_time_ms=0)
            
        recommendations = ml_engine.compute_similar_meals(
            target_meal_id=meal_id,
            candidate_meals=candidate_meals,
            top_n=5
        )
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/meals/{meal_id}/similar", response_model=RecommendationResponse)
async def get_similar_meals_post(meal_id: str, request: SimilarityRequest):
    """
    Stateless POST Endpoint: Accepts candidate meals from Android app.
    Maintains current Android integration functionality.
    """
    start_time = time.time()
    try:
        request.target_meal_id = meal_id
        
        recommendations = ml_engine.compute_similar_meals(
            target_meal_id=request.target_meal_id,
            candidate_meals=request.candidate_meals,
            top_n=request.top_n
        )
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/because-you-liked/{meal_id}", response_model=RecommendationResponse)
async def get_because_you_liked(meal_id: str, request: SimilarityRequest):
    start_time = time.time()
    try:
        recommendations = ml_engine.compute_similar_meals(
            target_meal_id=meal_id,
            candidate_meals=request.candidate_meals,
            top_n=request.top_n
        )
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
