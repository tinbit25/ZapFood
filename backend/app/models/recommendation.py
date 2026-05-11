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
    foodType: str = "NON_FASTING"
    dietType: str = "MEAT"
    spiceLevel: str = "MEDIUM"
    proteinLevel: str = "MEDIUM"
    mealTime: List[str] = []
    tags: List[str] = []
    ingredients: List[str] = []
    mealRegion: str = ""
    traditionalCategory: str = ""
    popularityScore: float = 0.0
    averageRating: float = 0.0

class UserFoodPreference(BaseModel):
    userId: str
    fastingMode: bool = False
    spiceTolerance: str = "MEDIUM"
    preferredBudgetRange: str = "Medium"
    dietaryPreferences: List[str] = []
    explicitFavoriteMeals: List[str] = []
    preferredMealTimes: List[str] = []
    
    favoriteMeals: List[str] = []
    dislikedMeals: List[str] = []
    favoriteVendors: List[str] = []
    favoriteCategories: List[str] = []
    
    mealTimePatterns: Dict[str, List[str]] = {}
    orderingPatterns: Dict[str, int] = {}
    cuisineAffinity: Dict[str, float] = {}
    fastingBehavior: str = "Occasional"

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
