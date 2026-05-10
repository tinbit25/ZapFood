from app.models.recommendation import Meal
from app.services.ml_similarity_engine import MLSimilarityEngine

ml_engine = MLSimilarityEngine()

def calculate_similarity_score(meal: Meal, candidate_meals: list[Meal], user_favorite_meals: list[str]) -> float:
    if not user_favorite_meals:
        return 0.5
        
    # Pick the top favorite meal as the target for similarity
    # Or average the similarity across all favorite meals
    total_score = 0.0
    valid_comparisons = 0
    
    for fav_meal_id in user_favorite_meals:
        results = ml_engine.compute_similar_meals(fav_meal_id, candidate_meals, top_n=len(candidate_meals))
        # Find the score for the current meal
        match = next((r for r in results if r.meal.id == meal.id), None)
        if match:
            total_score += match.final_score
            valid_comparisons += 1
            
    if valid_comparisons == 0:
        return 0.5
        
    return total_score / valid_comparisons
