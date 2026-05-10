from pydantic import BaseModel
from typing import List, Optional

class AIPersonalizedRequest(BaseModel):
    userId: str
    mealTime: Optional[str] = "lunch"
    fastingMode: Optional[bool] = False
    currentDay: Optional[str] = "Monday"
    weather: Optional[str] = "Sunny"

class AICartComboRequest(BaseModel):
    userId: str
    cartMealIds: List[str]

class ScoredMealResponse(BaseModel):
    mealId: str
    name: str
    imageUrl: str
    price: float
    vendorId: str
    matchScore: float
    reason: str
    tags: List[str]

class AIAnalyticsEventRequest(BaseModel):
    userId: str
    eventType: str # e.g. "recommendation_click", "combo_accepted", "checkout_conversion"
    mealId: Optional[str] = None
    recommendationContext: Optional[str] = None # e.g. "homefeed", "similar_meals", "cart"

class AIRecommendationResponse(BaseModel):
    recommendedMeals: List[ScoredMealResponse]
    generatedAt: str
    processingTimeMs: float
