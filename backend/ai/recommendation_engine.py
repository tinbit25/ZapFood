from typing import List
from app.models.recommendation import Meal, UserFoodPreference, ScoredMeal
from ai.scoring_engine import ScoringEngine

class RecommendationEngine:
    """Master engine orchestrating the scoring modules."""
    
    def __init__(self):
        self.scorer = ScoringEngine()
        
        self.WEIGHTS = {
            "preference": 0.30,
            "similarity": 0.15,
            "history": 0.15,
            "fasting": 0.15,
            "popularity": 0.10,
            "time": 0.10,
            "vendor": 0.05
        }

    def _generate_explanation(self, meal: Meal, breakdown: dict, current_day: str, pref: UserFoodPreference) -> str:
        # Generate human-readable explanation based on highest scoring components
        if breakdown["fasting"] == 1.0 and current_day in ["Wednesday", "Friday"]:
            return f"Recommended because you often order fasting foods on {current_day}s."
            
        if breakdown["time"] == 1.0:
            return f"A perfect choice for this time of day."
            
        if breakdown["history"] == 1.0:
            return "Recommended because you frequently order and enjoy this."
            
        if breakdown["preference"] > 0.8:
            return "Matches your spice tolerance and dietary preferences closely."
            
        if breakdown["similarity"] > 0.7:
            return "Recommended because it shares ingredients and spice profile with your favorites."
            
        if breakdown["popularity"] > 0.8:
            return "Highly rated and trending among other users."
            
        return "Tailored for you based on your unique tastes."

    def score_meals(self, candidate_meals: List[Meal], pref: UserFoodPreference, current_time: str, current_day: str) -> List[ScoredMeal]:
        scored_meals = []
        seen_names = set() # For diversity filtering
        
        for meal in candidate_meals:
            # Diversity Filter: Avoid showing identical meals repeatedly
            if meal.name.lower() in seen_names:
                continue
            
            pref_score = self.scorer.calculate_preference_score(meal, pref)
            sim_score = self.scorer.calculate_similarity_score(meal, pref, candidate_meals)
            hist_score = self.scorer.calculate_history_score(meal, pref)
            fast_score = self.scorer.calculate_fasting_score(meal, current_day, pref)
            time_score = self.scorer.calculate_time_score(meal, current_time)
            pop_score = self.scorer.calculate_popularity_score(meal)
            
            # Vendor affinity is partially handled in preference, but we could add a dedicated score
            vendor_score = 1.0 if meal.vendorId in pref.favoriteVendors else 0.5
            
            # Penalize heavily if it's a fasting day and the meal is meat
            if fast_score < 0:
                continue
            
            final_score = (
                (pref_score * self.WEIGHTS["preference"]) +
                (sim_score * self.WEIGHTS["similarity"]) +
                (hist_score * self.WEIGHTS["history"]) +
                (fast_score * self.WEIGHTS["fasting"]) +
                (time_score * self.WEIGHTS["time"]) +
                (pop_score * self.WEIGHTS["popularity"]) +
                (vendor_score * self.WEIGHTS["vendor"])
            )
                
            breakdown = {
                "preference": pref_score,
                "similarity": sim_score,
                "history": hist_score,
                "fasting": fast_score,
                "time": time_score,
                "popularity": pop_score,
                "vendor": vendor_score
            }
            
            reasoning = self._generate_explanation(meal, breakdown, current_day, pref)
            
            scored_meals.append(ScoredMeal(
                meal=meal,
                final_score=final_score,
                score_breakdown=breakdown,
                cultural_reasoning=reasoning
            ))
            
            seen_names.add(meal.name.lower())
            
        # Smart Ranking
        scored_meals.sort(key=lambda x: x.final_score, reverse=True)
        return scored_meals
