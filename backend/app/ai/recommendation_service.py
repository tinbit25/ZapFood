import logging
from typing import List, Dict, Any
from app.ai.gemini_client import GeminiClient
from app.ai.prompt_builder import EthiopianFoodPromptBuilder
from app.repositories.meal_repository import MealRepository
from app.repositories.behavior_repository import BehaviorRepository
from app.services.behavior_service import BehaviorService

logger = logging.getLogger("ai-rec-service")

class RecommendationService:
    def __init__(self):
        self.gemini = GeminiClient()
        self.meal_repo = MealRepository()
        self.behavior_repo = BehaviorRepository()
        self.behavior_service = BehaviorService(repository=self.behavior_repo)

    async def get_ai_recommendations(self, user_id: str, user_profile: Dict[str, Any]) -> Dict[str, Any]:
        try:
            # 1. Fetch available meals from Firestore
            all_meals = self.meal_repo.get_available_meals()
            
            # 2. Hybrid Step: Pre-Filter (Rule-Based Filtering)
            # This reduces token usage and prevents Gemini from breaking fundamental rules (like fasting)
            from datetime import datetime
            dt = datetime.now()
            weekday = dt.weekday()
            hour = dt.hour
            
            fasting_mode = weekday in [2, 4] or user_profile.get("fastingMode", False)
            
            meal_time = "late-night"
            if 6 <= hour < 11: meal_time = "breakfast"
            elif 11 <= hour < 16: meal_time = "lunch"
            elif 18 <= hour < 22: meal_time = "dinner"

            # Filter logic:
            filtered_meals_objs = []
            for m in all_meals:
                # Rule 1: Fasting Adherence
                if fasting_mode and not m.fastingFriendly:
                    continue
                
                # Rule 2: Meal Time Match (Allow lunch meals for dinner but not breakfast for lunch)
                # This is a simplified rule
                if meal_time == "breakfast" and "Breakfast" not in (m.mealTimes or []):
                    continue
                
                # Rule 3: Spice Level (Don't show Very Spicy to Low Tolerance users)
                if user_profile.get("spiceTolerance") == "low" and m.spiceLevel == "spicy":
                    continue
                    
                filtered_meals_objs.append(m)

            # Convert to dict for PromptBuilder
            candidate_meals = [
                {
                    "id": m.id,
                    "name": m.name,
                    "category": m.category,
                    "fastingFriendly": m.fastingFriendly,
                    "spiceLevel": m.spiceLevel,
                    "price": m.price,
                    "description": getattr(m, 'description', '')
                } for m in filtered_meals_objs[:15] # Limit candidates for cost/performance
            ]
            
            # 3. Fetch user behavior history for ranking context
            history = self.behavior_repo.get_user_behavior(user_id, limit=10)
            
            # 4. Construct context for Gemini with lightweight personalization
            patterns = self.behavior_service.get_user_patterns(user_id)
            preference_summary = self.behavior_service.build_preference_summary(patterns)

            user_habits = {
                "orderHistory": [h.get("mealId") for h in history if h.get("interactionType") == "PURCHASE"],
                "favoriteMeals": [h.get("mealId") for h in history if h.get("interactionType") == "FAVORITE"],
                "repeatedMeals": patterns.get("repeated_meal_ids", []),
                "topCategories": patterns.get("top_categories", []),
                "preferenceSummary": preference_summary,
                "spiceTolerance": user_profile.get("spiceTolerance", "medium"),
                "budgetPreference": user_profile.get("budgetPreference", "standard")
            }
            
            context = {
                "mealTime": meal_time,
                "fastingMode": fasting_mode,
                "dayOfWeek": dt.strftime("%A")
            }

            # 5. Gemini Step: Ranking & Reasoning
            # Gemini only sees VALID candidates and provides the final 'smart' layer
            prompt = EthiopianFoodPromptBuilder.build_recommendation_prompt(
                available_meals=candidate_meals,
                user_habits=user_habits,
                context=context
            )

            # Update PromptBuilder behavior (handled in instruction)
            response = self.gemini.generate_recommendations(prompt)

            # 6. Hybrid Validation & Final Scoring
            valid_ids = [m['id'] for m in candidate_meals]
            recommendations = []
            
            # Structure the output with scores and reasons
            for mid in response.get("recommendedMealIds", []):
                if mid in valid_ids:
                    meal_obj = next((m for m in candidate_meals if m['id'] == mid), None)
                    if meal_obj:
                        recommendations.append({
                            "mealId": mid,
                            "mealName": meal_obj['name'],
                            "score": 90 - (len(recommendations) * 5), # Simplified scoring
                            "reason": response.get("reasoning", "Matches your cultural preferences.")
                        })

            return {
                "recommendations": recommendations,
                "reasoning": response.get("reasoning", ""),
                "nutritionSummary": response.get("nutritionSummary", "")
            }

        except Exception as e:
            logger.error(f"Hybrid Recommendation failed: {e}")
            return {
                "recommendations": [],
                "reasoning": "System is currently prioritizing reliability. Please check traditional favorites.",
                "nutritionSummary": ""
            }
