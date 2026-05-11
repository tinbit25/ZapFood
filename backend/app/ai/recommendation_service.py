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
            if not all_meals:
                return self._get_fallback_recommendations("We couldn't find any meals right now. Showing popular Ethiopian favorites.")

            # 2. Hybrid Step: Pre-Filter & Categorization
            from datetime import datetime
            dt = datetime.now()
            weekday = dt.weekday()
            hour = dt.hour
            
            # Ethiopian fasting: Wednesday (2) and Friday (4)
            is_fasting_day = weekday in [2, 4]
            user_fasting_mode = user_profile.get("fastingMode", False)
            effective_fasting = is_fasting_day or user_fasting_mode
            
            # Meal Time Intelligence
            current_meal_time = "DINNER"
            if 5 <= hour < 10: current_meal_time = "BREAKFAST"
            elif 11 <= hour < 16: current_meal_time = "LUNCH"
            elif 16 <= hour < 19: current_meal_time = "SNACK"
            
            # Filter meals by rules
            smart_candidates = []
            fasting_meals = []
            popular_in_addis = sorted(all_meals, key=lambda x: x.popularityScore, reverse=True)

            for m in all_meals:
                # Rule 1: Fasting Adherence (Strict for Fasting category)
                is_fasting_friendly = m.fastingFriendly or "FASTING" in (m.tags or []) or "VEGAN" in (m.tags or [])
                if is_fasting_friendly:
                    fasting_meals.append(m)

                # Rule 2: Smart Selection (Personalized)
                # Adhere to user fasting preference
                if effective_fasting and not is_fasting_friendly:
                    continue
                
                # Spice Preference Check
                user_spice = user_profile.get("spicePreference", "MEDIUM").upper()
                meal_spice = m.spiceLevel.upper() if m.spiceLevel else "MEDIUM"
                if user_spice == "LOW" and meal_spice == "HIGH":
                    continue
                
                # Meal Time Preference (Soft filter for smart picks)
                # But for breakfast time, prioritize breakfast items
                if current_meal_time == "BREAKFAST" and "BREAKFAST" not in [t.upper() for t in (m.mealTime or [])]:
                    if len(smart_candidates) > 5: continue # Soft limit non-breakfast items in AM
                
                smart_candidates.append(m)

            # 3. Construct user context for Gemini ranking
            history = self.behavior_repo.get_user_behavior(user_id, limit=10)
            patterns = self.behavior_service.get_user_patterns(user_id)
            preference_summary = self.behavior_service.build_preference_summary(patterns)

            user_habits = {
                "orderHistory": [h.get("mealId") for h in history if h.get("interactionType") == "PURCHASE"],
                "favoriteMeals": user_profile.get("favoriteFoods", []),
                "repeatedMeals": patterns.get("repeated_meal_ids", []),
                "topCategories": user_profile.get("favoriteCategories", []),
                "preferenceSummary": preference_summary,
                "spicePreference": user_profile.get("spicePreference", "MEDIUM"),
                "budgetPreference": user_profile.get("budgetPreference", "STANDARD")
            }
            
            context = {
                "mealTime": current_meal_time,
                "fastingMode": effective_fasting,
                "dayOfWeek": dt.strftime("%A"),
                "isWednesdayOrFriday": is_fasting_day
            }

            # 4. Gemini Step: Smart Picks Ranking
            # We rank the top 15 candidates
            candidates_for_gemini = [
                {
                    "id": m.id,
                    "name": m.name,
                    "category": m.category,
                    "tags": m.tags,
                    "mealTime": m.mealTime,
                    "spiceLevel": m.spiceLevel,
                    "price": m.price,
                    "vendorName": m.vendorName
                } for m in smart_candidates[:15]
            ]
            
            prompt = EthiopianFoodPromptBuilder.build_recommendation_prompt(
                available_meals=candidates_for_gemini,
                user_habits=user_habits,
                context=context
            )

            response = self.gemini.generate_recommendations(prompt)
            
            # 5. Build Result Structure
            # Smart Picks
            smart_picks = []
            valid_ids = [m['id'] for m in candidates_for_gemini]
            for mid in response.get("recommendedMealIds", []):
                if mid in valid_ids:
                    meal_obj = next((m for m in smart_candidates if m.id == mid), None)
                    if meal_obj:
                        smart_picks.append({
                            "mealId": mid,
                            "mealName": meal_obj.name,
                            "score": 95 - (len(smart_picks) * 5),
                            "reason": response.get("reasoning", "Matches your taste.")
                        })

            # If Gemini fails or returns too few, fill with candidates
            if len(smart_picks) < 3:
                for m in smart_candidates:
                    if m.id not in [p['mealId'] for p in smart_picks]:
                        smart_picks.append({
                            "mealId": m.id,
                            "mealName": m.name,
                            "score": 70,
                            "reason": "Popular traditional choice."
                        })
                    if len(smart_picks) >= 6: break

            # Format categories for UI
            return {
                "smartPicks": smart_picks[:10],
                "fastingMeals": [self._map_to_scored_meal(m, "Today's fasting pick.") for m in fasting_meals[:8]],
                "popularInAddis": [self._map_to_scored_meal(m, f"Popular at {m.vendorName}") for m in popular_in_addis[:8]],
                "reasoning": response.get("reasoning", "Based on your cultural preferences and current time."),
                "nutritionSummary": response.get("nutritionSummary", "")
            }

        except Exception as e:
            logger.error(f"Smart Recommendation failed: {e}")
            return self._get_fallback_recommendations()

    def _map_to_scored_meal(self, meal, reason: str) -> Dict[str, Any]:
        return {
            "mealId": meal.id,
            "mealName": meal.name,
            "score": 85,
            "reason": reason
        }

    def _get_fallback_recommendations(self, reason: str = "System is currently prioritizing reliability.") -> Dict[str, Any]:
        # Simple fallback to popular meals
        all_meals = self.meal_repo.get_available_meals()
        popular = sorted(all_meals, key=lambda x: x.popularityScore, reverse=True)[:6]
        return {
            "smartPicks": [self._map_to_scored_meal(m, "Highly rated by others.") for m in popular],
            "fastingMeals": [],
            "popularInAddis": [self._map_to_scored_meal(m, "Community favorite.") for m in popular],
            "reasoning": reason,
            "nutritionSummary": ""
        }
