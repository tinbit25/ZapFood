from app.models.recommendation import Meal, UserFoodPreference

def calculate_fasting_score(meal: Meal, user: UserFoodPreference, day_of_week: str) -> float:
    is_fasting_day = day_of_week in ["Wednesday", "Friday"]
    
    # User is strict faster
    if user.fastingBehavior == "Strict":
        if is_fasting_day:
            return 1.0 if meal.fastingFriendly else 0.0
        else:
            return 0.5 # Doesn't care much, but can eat fasting food
            
    # User is occasional faster
    if user.fastingBehavior == "Occasional":
        if is_fasting_day:
            return 0.8 if meal.fastingFriendly else 0.2
        else:
            return 0.5
            
    # User doesn't fast
    if is_fasting_day:
        return 0.5 # Normal
    
    return 0.5
