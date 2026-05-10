from pydantic import BaseModel
from typing import List, Dict, Optional
from app.models.recommendation import Meal

class CartContext(BaseModel):
    cart_meals: List[Meal]
    time_of_day: str = "Lunch" # Morning, Afternoon, Evening, Late Night
    day_of_week: str = "Monday"
    weather: str = "Clear" # Clear, Rainy, Hot, Cold
    user_id: Optional[str] = None

class ComboRequest(BaseModel):
    context: CartContext
    candidate_meals: List[Meal]
    top_n: int = 3

class ComboRecommendation(BaseModel):
    meal: Meal
    match_score: float
    upsell_reason: str
