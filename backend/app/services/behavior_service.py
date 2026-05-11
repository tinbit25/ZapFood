import logging
from datetime import datetime
from typing import List, Optional, Dict, Any
from collections import Counter
from app.models.behavior import BehaviorEvent
from app.repositories.behavior_repository import BehaviorRepository

logger = logging.getLogger("behavior-service")

class BehaviorService:
    def __init__(self, repository: BehaviorRepository = None):
        self.repository = repository or BehaviorRepository()

    def process_event(self, event: BehaviorEvent) -> bool:
        # Enrich event with Ethiopian cultural context
        dt = datetime.fromtimestamp(event.timestamp / 1000.0)
        
        # 1. Detect Meal Time
        hour = dt.hour
        if 6 <= hour < 11:
            event.mealTime = "breakfast"
        elif 11 <= hour < 15:
            event.mealTime = "lunch"
        elif 18 <= hour < 22:
            event.mealTime = "dinner"
        else:
            event.mealTime = "late-night"

        # 2. Detect Fasting Day (Wednesday and Friday)
        # 0=Monday, 2=Wednesday, 4=Friday
        weekday = dt.weekday()
        if weekday in [2, 4]:
            event.fastingRelevant = True
        else:
            event.fastingRelevant = False

        return self.repository.add_event(event)

    def get_user_patterns(self, user_id: str) -> Dict[str, Any]:
        """
        Aggregate patterns for AI reasoning context without complex ML.
        Tracks: repeated orders, favorites, categories, fasting habits.
        """
        behaviors = self.repository.get_user_behavior(user_id, limit=50)
        
        if not behaviors:
            return {}

        categories = []
        meal_times = []
        purchased_meal_ids = []
        fasting_day_interactions = 0
        total_fasting_days = 0
        
        for b in behaviors:
            if b.get("mealCategory"):
                categories.append(b["mealCategory"])
            if b.get("mealTime"):
                meal_times.append(b["mealTime"])
            if b.get("interactionType") == "PURCHASE" and b.get("mealId"):
                purchased_meal_ids.append(b["mealId"])
            
            # Fasting habit detection
            if b.get("fastingRelevant"):
                total_fasting_days += 1
                # If they interacted with a fasting-friendly meal on a fasting day
                if b.get("metadata", {}).get("isFastingChoice"):
                    fasting_day_interactions += 1

        # Calculate repeated orders
        meal_counts = Counter(purchased_meal_ids)
        repeated_meals = [mid for mid, count in meal_counts.items() if count > 1]

        patterns = {
            "top_categories": [cat for cat, count in Counter(categories).most_common(3)],
            "favorite_meal_times": [mt for mt, count in Counter(meal_times).most_common(2)],
            "repeated_meal_ids": repeated_meals,
            "fasting_adherence_score": (fasting_day_interactions / total_fasting_days) if total_fasting_days > 0 else 0,
            "total_purchases": len(purchased_meal_ids)
        }
        return patterns

    def build_preference_summary(self, patterns: Dict[str, Any]) -> str:
        """
        Converts raw patterns into a natural language summary for Gemini.
        """
        if not patterns:
            return "New user with no historical habits yet."

        summary_parts = []
        
        if patterns.get("top_categories"):
            summary_parts.append(f"Prefers {', '.join(patterns['top_categories'])}.")
        
        if patterns.get("favorite_meal_times"):
            summary_parts.append(f"Usually orders during {', '.join(patterns['favorite_meal_times'])}.")
            
        if patterns.get("fasting_adherence_score", 0) > 0.7:
            summary_parts.append("Strictly follows Ethiopian fasting traditions on Wednesdays and Fridays.")
        elif patterns.get("fasting_adherence_score", 0) > 0.3:
            summary_parts.append("Occasionally chooses fasting meals.")

        if patterns.get("repeated_meal_ids"):
            summary_parts.append(f"Has repeated orders for {len(patterns['repeated_meal_ids'])} specific dishes.")

        return " ".join(summary_parts)
