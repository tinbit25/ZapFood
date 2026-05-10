from app.models.recommendation import Meal, UserFoodPreference
from ai.similarity_engine import MLSimilarityEngine
from ai.ethiopian_food_knowledge import CATEGORIES, CULTURAL_RULES, INGREDIENT_INTELLIGENCE

class ScoringEngine:
    """Consolidated engine for scoring meals based on user preferences, history, and context."""
    
    def __init__(self):
        self.ml_similarity_engine = MLSimilarityEngine()

    def calculate_preference_score(self, meal: Meal, pref: UserFoodPreference) -> float:
        score = 0.0
        meal_name = meal.name.lower()
        
        if pref.fastingMode and meal.fastingFriendly:
            score += 0.4
            
        # Ingredient intelligence matching
        ing_intel = INGREDIENT_INTELLIGENCE.get(meal_name)
        if ing_intel:
            if ing_intel["spice_level"] == pref.spiceTolerance:
                score += 0.2
        elif meal.spiceLevel == pref.spiceTolerance:
            score += 0.2
            
        if meal.category in pref.frequentlyOrderedCategories:
            score += 0.2
            
        if meal.vendorId in pref.favoriteVendors:
            score += 0.2
            
        return min(1.0, score)

    def calculate_similarity_score(self, candidate: Meal, pref: UserFoodPreference, candidate_meals: list) -> float:
        if not pref.favoriteMeals:
            return 0.0
            
        total_score = 0.0
        for fav_id in pref.favoriteMeals:
            recommendations = self.ml_similarity_engine.compute_similar_meals(
                target_meal_id=fav_id,
                candidate_meals=candidate_meals,
                top_n=len(candidate_meals)
            )
            for rec in recommendations:
                if rec.meal.id == candidate.id:
                    total_score += rec.final_score
                    break
                    
        return min(1.0, total_score / len(pref.favoriteMeals))

    def calculate_history_score(self, meal: Meal, pref: UserFoodPreference) -> float:
        if meal.id in pref.favoriteMeals:
            return 1.0
        if meal.id in pref.dislikedMeals:
            return -1.0
        return 0.0

    def calculate_fasting_score(self, meal: Meal, current_day: str, pref: UserFoodPreference) -> float:
        fasting_days = CULTURAL_RULES["fasting_days"]["days"]
        meal_name = meal.name.lower()
        
        if current_day in fasting_days:
            if any(fast_meal in meal_name for fast_meal in CULTURAL_RULES["fasting_days"]["prioritize"]):
                return 1.0
            if any(meat_meal in meal_name for meat_meal in CULTURAL_RULES["fasting_days"]["demote"]):
                return -1.0
            if meal.fastingFriendly:
                return 0.8
            else:
                return -0.5 if pref.fastingMode else 0.0
                
        return 0.5 if meal.fastingFriendly else 1.0

    def calculate_time_score(self, meal: Meal, current_time: str) -> float:
        meal_name = meal.name.lower()
        
        # Morning rules
        if current_time in CULTURAL_RULES["morning"]["times"]:
            if any(b_meal in meal_name for b_meal in CULTURAL_RULES["morning"]["prioritize"]):
                return 1.0
            if current_time in meal.mealTimes:
                return 0.8
            return 0.0
            
        # Evening rules
        if current_time in ["Evening", "Dinner"]:
            if any(t in meal_name for t in ["tibs", "beyaynetu"]):
                return 1.0
                
        if current_time in meal.mealTimes:
            return 0.8
            
        return 0.2

    def calculate_popularity_score(self, meal: Meal) -> float:
        return min(1.0, meal.popularityScore / 100.0)
