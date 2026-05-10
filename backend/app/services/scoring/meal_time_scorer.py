from app.models.recommendation import Meal

def calculate_meal_time_score(meal: Meal, time_of_day: str) -> float:
    if not time_of_day:
        return 0.5
        
    time_map = {
        "Morning": "BREAKFAST",
        "Afternoon": "LUNCH",
        "Evening": "DINNER",
        "Late Night": "SNACK"
    }
    
    expected_time = time_map.get(time_of_day, "")
    
    # Direct meal time match
    if expected_time in [mt.upper() for mt in meal.mealTime]:
        return 1.0
        
    # Cultural fallbacks
    lower_name = meal.name.lower()
    if time_of_day == "Morning" and any(x in lower_name for x in ["ful", "chechebsa", "kinche", "firfir"]):
        return 1.0
    if time_of_day == "Evening" and any(x in lower_name for x in ["tibs", "beyaynetu"]):
        return 0.8
        
    return 0.2
