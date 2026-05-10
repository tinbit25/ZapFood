from app.models.recommendation import Meal

def calculate_popularity_score(meal: Meal, max_popularity_in_dataset: float = 100.0) -> float:
    # Basic normalization
    if max_popularity_in_dataset <= 0:
        return 0.0
    return min(1.0, meal.popularityScore / max_popularity_in_dataset)
