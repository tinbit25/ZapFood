from typing import List, Dict, Any
from app.models.recommendation import Meal, UserFoodPreference, ScoredMeal, RecommendationRequest
from app.services.scoring.preference_scorer import calculate_preference_score
from app.services.scoring.fasting_scorer import calculate_fasting_score
from app.services.scoring.meal_time_scorer import calculate_meal_time_score
from app.services.scoring.history_scorer import calculate_history_score
from app.services.scoring.popularity_scorer import calculate_popularity_score
from app.services.scoring.similarity_scorer import calculate_similarity_score

class RecommendationEngine:
    def __init__(self):
        # Weights
        self.weights = {
            "preference": 0.35,
            "similarity": 0.20,
            "history": 0.15,
            "fasting": 0.15,
            "popularity": 0.10,
            "meal_time": 0.05
        }

    def generate_recommendations(self, request: RecommendationRequest) -> List[ScoredMeal]:
        user = request.user_preference
        candidates = request.candidate_meals
        day_of_week = request.current_day_of_week or "Monday"
        time_of_day = request.current_time_of_day or "Lunch"
        
        # 1. Filter out disliked or invalid
        valid_candidates = [m for m in candidates if m.id not in user.dislikedMeals]
        
        # Find max popularity for normalization
        max_pop = max([m.popularityScore for m in valid_candidates]) if valid_candidates else 1.0
        if max_pop == 0:
            max_pop = 1.0

        scored_meals = []
        for meal in valid_candidates:
            # 2. Calculate individual scores
            pref_score = calculate_preference_score(meal, user)
            sim_score = calculate_similarity_score(meal, candidates, user.favoriteMeals)
            hist_score = calculate_history_score(meal, user)
            fasting_score = calculate_fasting_score(meal, user, day_of_week)
            pop_score = calculate_popularity_score(meal, max_pop)
            time_score = calculate_meal_time_score(meal, time_of_day)

            # 3. Apply weights
            breakdown = {
                "preference": pref_score * self.weights["preference"],
                "similarity": sim_score * self.weights["similarity"],
                "history": hist_score * self.weights["history"],
                "fasting": fasting_score * self.weights["fasting"],
                "popularity": pop_score * self.weights["popularity"],
                "meal_time": time_score * self.weights["meal_time"]
            }
            
            final_score = sum(breakdown.values())
            
            # 4. Generate reasoning
            reasoning = self._generate_reasoning(meal, breakdown, user, day_of_week, time_of_day)
            
            scored_meals.append(ScoredMeal(
                meal=meal,
                final_score=final_score,
                score_breakdown=breakdown,
                cultural_reasoning=reasoning
            ))
            
        # 5. Sort descending by score
        scored_meals.sort(key=lambda x: x.final_score, reverse=True)
        return scored_meals

    def _generate_reasoning(self, meal: Meal, breakdown: Dict[str, float], user: UserFoodPreference, day_of_week: str, time_of_day: str) -> str:
        # Find highest contributing factor
        highest_factor = max(breakdown, key=breakdown.get)
        
        if highest_factor == "fasting" and day_of_week in ["Wednesday", "Friday"] and meal.fastingFriendly:
            return f"Recommended because today is a fasting day ({day_of_week}) and this is fasting-friendly."
        elif highest_factor == "preference":
            if meal.category in user.favoriteCategories:
                return f"Recommended because you prefer {meal.category}."
            return "Recommended based on your spice and dietary preferences."
        elif highest_factor == "history":
            return "Recommended because you frequently order this or similar items from this vendor."
        elif highest_factor == "meal_time":
            return f"A perfect choice for {time_of_day}."
        elif highest_factor == "popularity":
            return "Currently trending in your area."
        else:
            return "Recommended based on your overall flavor profile."
