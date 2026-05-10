import time
from fastapi import APIRouter, HTTPException
from app.models.recommendation import RecommendationRequest, RecommendationResponse
from ai.recommendation_engine import RecommendationEngine

router = APIRouter(prefix="/recommendations", tags=["Recommendations"])
engine = RecommendationEngine()

@router.post("/personalized", response_model=RecommendationResponse)
async def get_personalized_recommendations(request: RecommendationRequest):
    start_time = time.time()
    try:
        recommendations = engine.score_meals(
            candidate_meals=request.candidate_meals,
            pref=request.user_preference,
            current_time=request.current_time_of_day or "Lunch",
            current_day=request.current_day_of_week or "Monday"
        )
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/homefeed", response_model=RecommendationResponse)
async def get_homefeed_recommendations(request: RecommendationRequest):
    start_time = time.time()
    try:
        recommendations = engine.score_meals(
            candidate_meals=request.candidate_meals,
            pref=request.user_preference,
            current_time=request.current_time_of_day or "Lunch",
            current_day=request.current_day_of_week or "Monday"
        )
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/trending", response_model=RecommendationResponse)
async def get_trending_recommendations(request: RecommendationRequest):
    start_time = time.time()
    try:
        original_weights = engine.WEIGHTS.copy()
        engine.WEIGHTS = {
            "preference": 0.10,
            "similarity": 0.05,
            "history": 0.05,
            "fasting": 0.0,
            "popularity": 0.80,
            "time": 0.0
        }
        
        recommendations = engine.score_meals(
            candidate_meals=request.candidate_meals,
            pref=request.user_preference,
            current_time=request.current_time_of_day or "Lunch",
            current_day=request.current_day_of_week or "Monday"
        )
        engine.WEIGHTS = original_weights
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/fasting", response_model=RecommendationResponse)
async def get_fasting_recommendations(request: RecommendationRequest):
    start_time = time.time()
    try:
        request.user_preference.fastingMode = True
        
        original_weights = engine.WEIGHTS.copy()
        engine.WEIGHTS = {
            "preference": 0.20,
            "similarity": 0.10,
            "history": 0.10,
            "fasting": 0.50,
            "popularity": 0.05,
            "time": 0.05
        }
        
        fasting_candidates = [m for m in request.candidate_meals if m.fastingFriendly]
        
        recommendations = engine.score_meals(
            candidate_meals=fasting_candidates,
            pref=request.user_preference,
            current_time=request.current_time_of_day or "Lunch",
            current_day=request.current_day_of_week or "Wednesday" # Force a fasting day
        )
        engine.WEIGHTS = original_weights
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
