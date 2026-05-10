import time
from fastapi import APIRouter, HTTPException
from typing import List
from app.models.recommendation import RecommendationRequest, RecommendationResponse, ScoredMeal
from app.services.recommendation_engine import RecommendationEngine

router = APIRouter(prefix="/recommendations", tags=["Recommendations"])
engine = RecommendationEngine()

@router.post("/personalized", response_model=RecommendationResponse)
async def get_personalized_recommendations(request: RecommendationRequest):
    start_time = time.time()
    try:
        recommendations = engine.generate_recommendations(request)
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/homefeed", response_model=RecommendationResponse)
async def get_homefeed_recommendations(request: RecommendationRequest):
    # For homefeed, we might balance it slightly differently, but reuse engine for now
    start_time = time.time()
    try:
        recommendations = engine.generate_recommendations(request)
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
        # Override weights to heavily favor popularity
        original_weights = engine.weights.copy()
        engine.weights = {
            "preference": 0.10,
            "similarity": 0.05,
            "history": 0.05,
            "fasting": 0.0,
            "popularity": 0.80,
            "meal_time": 0.0
        }
        
        recommendations = engine.generate_recommendations(request)
        engine.weights = original_weights # Restore
        
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
        # Override user profile to enforce fasting mode, regardless of explicit profile
        request.user_preference.fastingMode = True
        request.user_preference.fastingBehavior = "Strict"
        
        # Override weights
        original_weights = engine.weights.copy()
        engine.weights = {
            "preference": 0.20,
            "similarity": 0.10,
            "history": 0.10,
            "fasting": 0.50,
            "popularity": 0.05,
            "meal_time": 0.05
        }
        
        # We can also filter candidates explicitly before passing
        fasting_candidates = [m for m in request.candidate_meals if m.fastingFriendly]
        request.candidate_meals = fasting_candidates
        
        recommendations = engine.generate_recommendations(request)
        engine.weights = original_weights # Restore
        
        end_time = time.time()
        return RecommendationResponse(
            recommendations=recommendations,
            processing_time_ms=(end_time - start_time) * 1000
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
