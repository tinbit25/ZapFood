from pydantic import BaseModel, Field
from typing import List, Dict, Optional, Any

class Meal(BaseModel):
    id: str
    name: str
    price: float
    category: str
    vendorId: str
    vendorName: str
    fastingFriendly: bool = False
    veganFriendly: bool = False
    foodType: str = "NON_FASTING" # FASTING, NON_FASTING
    dietType: str = "MEAT" # MEAT, VEGAN, DAIRY
    spiceLevel: str = "MEDIUM" # LOW, MEDIUM, HIGH
    mealTime: List[str] = [] # BREAKFAST, LUNCH, DINNER
    tags: List[str] = [] # TRADITIONAL, SPICY, VEGAN, etc.
    ingredients: List[str] = []
    mealRegion: str = ""
    traditionalCategory: str = ""
    popularityScore: float = 0.0
    averageRating: float = 0.0

class UserFoodPreference(BaseModel):
    userId: str
    fastingMode: bool = False
    spicePreference: str = "MEDIUM" # LOW, MEDIUM, HIGH
    dietaryType: str = "ANY" # VEGAN, NON_VEGAN, ANY
    budgetPreference: str = "STANDARD" # BUDGET, STANDARD, PREMIUM
    favoriteFoods: List[str] = []
    preferredMealTime: str = "ANY"
    favoriteVendors: List[str] = []
    favoriteCategories: List[str] = []
    lastUpdated: Optional[int] = None

class RecommendationRequest(BaseModel):
    user_preference: UserFoodPreference
    candidate_meals: List[Meal]
    current_time_of_day: Optional[str] = None # Morning, Afternoon, Evening, Late Night
    current_day_of_week: Optional[str] = None # e.g. Wednesday, Friday

class ScoredMeal(BaseModel):
    meal: Meal
    final_score: float
    score_breakdown: Dict[str, float]
    cultural_reasoning: str

class RecommendationResponse(BaseModel):
    recommendations: List[ScoredMeal]
    processing_time_ms: float

class SimilarityRequest(BaseModel):
    target_meal_id: str
    candidate_meals: List[Meal]
    top_n: int = 5
