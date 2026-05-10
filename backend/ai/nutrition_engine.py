from typing import List, Dict, Optional
from app.models.recommendation import Meal

# Nutrition definitions mapping
NUTRITION_PROFILES = {
    "tibs": {"protein": "high", "oil": "medium", "carbs": "low", "veg": "low"},
    "derek tibs": {"protein": "high", "oil": "low", "carbs": "low", "veg": "low"}, # Grilled Tibs
    "shiro": {"protein": "medium", "oil": "medium", "carbs": "medium", "veg": "low"},
    "misir": {"protein": "high", "oil": "medium", "carbs": "medium", "veg": "medium"}, # Lentil meals
    "kik alicha": {"protein": "medium", "oil": "low", "carbs": "medium", "veg": "medium"},
    "gomen": {"protein": "low", "oil": "low", "carbs": "low", "veg": "high"},
    "salata": {"protein": "low", "oil": "low", "carbs": "low", "veg": "high"},
    "kitfo": {"protein": "high", "oil": "high", "carbs": "low", "veg": "low"},
    "doro wat": {"protein": "high", "oil": "high", "carbs": "medium", "veg": "low"},
    "beyaynetu": {"protein": "medium", "oil": "medium", "carbs": "high", "veg": "high"},
    "ful": {"protein": "high", "oil": "high", "carbs": "high", "veg": "low"},
    "chechebsa": {"protein": "low", "oil": "high", "carbs": "high", "veg": "low"}
}

class NutritionEngine:
    def __init__(self):
        self.health_goals = ["balanced_eating", "high_protein", "low_oil", "fasting_healthy"]

    def analyze_meal(self, meal: Meal) -> Dict[str, str]:
        """Returns the nutritional profile of the meal."""
        meal_name = meal.name.lower()
        
        # Default profile if unknown
        profile = {"protein": "medium", "oil": "medium", "carbs": "medium", "veg": "medium"}
        
        for key, p in NUTRITION_PROFILES.items():
            if key in meal_name:
                profile = p
                break
                
        # Supplement with known properties
        profile["spice"] = meal.spiceLevel
        profile["fasting"] = meal.fastingFriendly
        return profile

    def generate_nutritional_labels(self, meal: Meal) -> List[str]:
        """Generates friendly nutritional labels rather than hard calories."""
        labels = []
        profile = self.analyze_meal(meal)
        
        if profile["protein"] == "high":
            labels.append("High Protein")
            
        if profile["oil"] == "low":
            labels.append("Low Oil")
            
        if meal.fastingFriendly or profile["veg"] == "high":
            labels.append("Vegetarian / Fasting Friendly")
            
        if profile["protein"] == "high" and profile["oil"] == "low":
            labels.append("Healthy Choice")
            
        return labels

    def score_for_health_goal(self, meal: Meal, goal: str) -> float:
        """Scores a meal positively or negatively against a user's health goal."""
        profile = self.analyze_meal(meal)
        score = 0.0
        
        if goal == "high_protein" and profile["protein"] == "high":
            score += 1.0
            
        if goal == "low_oil":
            if profile["oil"] == "low":
                score += 1.0
            elif profile["oil"] == "high":
                score -= 1.0
                
        if goal == "balanced_eating":
            if profile["veg"] == "high" or profile["oil"] == "low":
                score += 0.5
            if profile["protein"] == "high":
                score += 0.5
                
        if goal == "fasting_healthy":
            if meal.fastingFriendly and profile["oil"] != "high":
                score += 1.0
                
        return score

    def constrain_excessive_oil(self, recent_orders: List[Meal], candidate_meals: List[Meal]) -> List[Meal]:
        """Avoids repeatedly recommending high-oil meals if the user has been eating them."""
        oily_count = 0
        for m in recent_orders:
            if self.analyze_meal(m)["oil"] == "high":
                oily_count += 1
                
        # If more than 2 out of recent orders were high oil, filter heavily
        if oily_count >= 2:
            filtered = []
            for m in candidate_meals:
                if self.analyze_meal(m)["oil"] != "high":
                    filtered.append(m)
            return filtered if filtered else candidate_meals
            
        return candidate_meals
