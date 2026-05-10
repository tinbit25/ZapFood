from app.models.recommendation import Meal, UserFoodPreference

def calculate_history_score(meal: Meal, user: UserFoodPreference) -> float:
    score = 0.0
    
    # Vendor affinity
    if meal.vendorId in user.favoriteVendors:
        score += 0.4
        
    # Implicit order count
    if meal.id in user.favoriteMeals:
        score += 0.5
        
    # Explicit favorite
    if meal.id in user.explicitFavoriteMeals:
        score += 0.6
        
    # Cuisine affinity
    if meal.mealRegion in user.cuisineAffinity:
        affinity = user.cuisineAffinity[meal.mealRegion]
        score += affinity * 0.2
        
    return min(1.0, score)
