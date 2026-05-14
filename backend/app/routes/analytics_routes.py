import logging
from fastapi import APIRouter, Depends, HTTPException
from app.models.behavior import BehaviorEvent
from app.services.behavior_service import BehaviorService
from app.ai.recommendation_service import RecommendationService
from app.config import get_settings

router = APIRouter(prefix="/api", tags=["Analytics & AI"])
logger = logging.getLogger("analytics-router")

def get_behavior_service():
    return BehaviorService()

@router.post("/analytics/track", response_model=None)
async def track_behavior(
    event: BehaviorEvent,
    service: BehaviorService = Depends(get_behavior_service)
):
    """
    Log a user behavior event.
    """
    success = service.process_event(event)
    if not success:
        raise HTTPException(status_code=500, detail="Failed to log event")
    return {"status": "ok"}

from app.repositories.user_repository import UserRepository

def get_recommendation_service():
    return RecommendationService()

def get_user_repository():
    return UserRepository()

@router.get("/recommendations/ai/{user_id}", response_model=None)
async def get_ai_recommendations(
    user_id: str,
    service: RecommendationService = Depends(get_recommendation_service),
    user_repo: UserRepository = Depends(get_user_repository)
):
    """
    Get intelligent food recommendations powered by Gemini AI.
    """
    # Fetch real user preferences from repository
    preferences = user_repo.get_user_preferences(user_id)
    user_profile = {
        "displayName": f"User {user_id}",
        "spiceTolerance": preferences.spicePreference,
        "favoriteCategories": preferences.favoriteCategories,
        "fastingMode": preferences.fastingMode
    }
    
    result = await service.get_ai_recommendations(user_id, user_profile)
    return result
