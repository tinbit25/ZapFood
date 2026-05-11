import logging
import time
from typing import List, Optional
from app.models.recommendation import Meal
from app.services.firebase_client import get_firestore_client, is_firebase_connected

logger = logging.getLogger(__name__)

class MealRepository:
    def __init__(self):
        # In-memory cache for fast AI scoring
        self._cache: List[Meal] = []
        self._last_fetch_time: float = 0
        self._cache_ttl = 300 # 5 minutes

    def _fetch_meals_from_firestore(self) -> List[Meal]:
        if not is_firebase_connected():
            logger.warning("Firebase not connected. Returning empty meals.")
            return []
            
        try:
            db = get_firestore_client()
            meals_ref = db.collection("meals").where("isAvailable", "==", True)
            docs = meals_ref.stream()
            
            meals = []
            for doc in docs:
                data = doc.to_dict()
                try:
                    # Map Firestore data to Meal model
                    meals.append(Meal(
                        id=doc.id,
                        name=data.get("name", ""),
                        vendorId=data.get("vendorId", ""),
                        vendorName=data.get("vendorName", "ZapFood Vendor"),
                        price=float(data.get("price", 0.0)),
                        category=data.get("category", "traditional"),
                        tags=data.get("tags", []),
                        ingredients=data.get("ingredients", []),
                        spiceLevel=data.get("spiceLevel", "medium"),
                        fastingFriendly=data.get("fastingFriendly", False),
                        veganFriendly=data.get("veganFriendly", False),
                        popularityScore=data.get("popularityScore", 50),
                        mealTime=data.get("mealTimes", ["Lunch", "Dinner"]),
                        imageUrl=data.get("imageUrl", "")
                    ))
                except Exception as map_err:
                    logger.error(f"Error mapping meal {doc.id}: {map_err}")
            return meals
        except Exception as e:
            logger.error(f"Failed to fetch meals from Firestore: {e}")
            return []

    def get_available_meals(self) -> List[Meal]:
        current_time = time.time()
        # Return cache if valid
        if self._cache and (current_time - self._last_fetch_time) < self._cache_ttl:
            return self._cache
            
        # Fetch fresh data
        fresh_meals = self._fetch_meals_from_firestore()
        if fresh_meals:
            self._cache = fresh_meals
            self._last_fetch_time = current_time
            
        return self._cache
        
    def get_meals_by_ids(self, meal_ids: List[str]) -> List[Meal]:
        meals = self.get_available_meals()
        return [m for m in meals if m.id in meal_ids]
