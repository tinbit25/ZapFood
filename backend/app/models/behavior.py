from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime

class BehaviorEvent(BaseModel):
    id: str
    userId: str
    mealId: Optional[str] = None
    vendorId: Optional[str] = None
    interactionType: str
    timestamp: int
    sessionId: str
    deviceType: str = "android"
    mealCategory: Optional[str] = None
    mealTags: List[str] = []
    fastingRelevant: Optional[bool] = None
    mealTime: Optional[str] = None
    metadata: Optional[dict] = {}

class AnalyticsSummary(BaseModel):
    totalInteractions: int
    topMeals: List[dict]
    trendingVendors: List[dict]
    fastingPatterns: dict
    mealTimeDistribution: dict
