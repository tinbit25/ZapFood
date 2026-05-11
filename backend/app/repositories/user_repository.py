import logging
from typing import Optional, List
from app.models.recommendation import UserFoodPreference
from app.services.firebase_client import get_firestore_client, is_firebase_connected

logger = logging.getLogger(__name__)

class UserRepository:
    def _get_default_preferences(self) -> UserFoodPreference:
        return UserFoodPreference(
            userId="guest",
            favoriteCategories=["traditional", "breakfast"],
            favoriteVendors=[],
            spicePreference="MEDIUM",
            fastingMode=False,
            dietaryType="ANY",
            budgetPreference="STANDARD",
            favoriteFoods=[],
            preferredMealTime="ANY"
        )

    def get_user_preferences(self, user_id: str) -> UserFoodPreference:
        if not is_firebase_connected():
            return self._get_default_preferences()
            
        try:
            db = get_firestore_client()
            user_doc = db.collection("users").document(user_id).get()
            
            if not user_doc.exists:
                return self._get_default_preferences()
                
            data = user_doc.to_dict()
            prefs_data = data.get("preferences", {})
            
            return UserFoodPreference(
                userId=user_id,
                favoriteCategories=prefs_data.get("favoriteCategories", ["traditional"]),
                favoriteVendors=prefs_data.get("favoriteVendors", []),
                spicePreference=prefs_data.get("spicePreference", "MEDIUM"),
                fastingMode=prefs_data.get("fastingMode", False),
                dietaryType=prefs_data.get("dietaryType", "ANY"),
                budgetPreference=prefs_data.get("budgetPreference", "STANDARD"),
                favoriteFoods=prefs_data.get("favoriteFoods", []),
                preferredMealTime=prefs_data.get("preferredMealTime", "ANY"),
                lastUpdated=prefs_data.get("lastUpdated")
            )
        except Exception as e:
            logger.error(f"Failed to fetch user preferences from Firestore: {e}")
            return self._get_default_preferences()
