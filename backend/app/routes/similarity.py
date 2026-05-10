import time
from fastapi import APIRouter, HTTPException
from app.models.recommendation import SimilarityRequest, RecommendationResponse
from app.services.ml_similarity_engine import MLSimilarityEngine

router = APIRouter(prefix="/similarity", tags=["Similarity ML"])
ml_engine = MLSimilarityEngine()

@router.post("/meals/{meal_id}/similar", response_model=RecommendationResponse)
async def get_similar_meals(meal_id: str, request: SimilarityRequest):
    start_time = time.time()
    try:
        # Override the target_meal_id with the path param
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
    # Logically similar, but we might tweak top_n or weights later if we wanted.
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
