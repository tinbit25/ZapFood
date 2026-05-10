from app.models.recommendation import Meal, UserFoodPreference

def calculate_preference_score(meal: Meal, user: UserFoodPreference) -> float:
    score = 0.0
    
    # Spice level match
    if user.spiceTolerance == meal.spiceLevel:
        score += 0.3
    elif (user.spiceTolerance == "MEDIUM" and meal.spiceLevel in ["MILD", "SPICY"]):
        score += 0.1
        
    # Dietary Match
    if meal.veganFriendly and "vegan" in [d.lower() for d in user.dietaryPreferences]:
        score += 0.4
        
    # Categories
    if meal.category in user.favoriteCategories:
        score += 0.3
        
    return min(1.0, score)
