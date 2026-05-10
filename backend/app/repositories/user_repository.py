import logging
from typing import Optional, List
from app.models.recommendation import UserFoodPreference
from app.services.firebase_client import get_firestore_client, is_firebase_connected

logger = logging.getLogger(__name__)

class UserRepository:
    def _get_default_preferences(self) -> UserFoodPreference:
        return UserFoodPreference(
            favoriteCategories=["traditional", "breakfast"],
            favoriteVendors=[],
            spiceTolerance="medium",
            fastingMode=False,
            fastingBehavior="None",
            favoriteMeals=[],
            dislikedMeals=[],
            frequentlyOrderedCategories=[]
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
            
            # Fetch recent orders to determine behavioral patterns
            recent_orders = []
            try:
                orders_ref = db.collection("users").document(user_id).collection("orders")
                orders_docs = orders_ref.order_by("createdAt", direction="DESCENDING").limit(10).stream()
                for o in orders_docs:
                    recent_orders.append(o.to_dict())
            except Exception as e:
                logger.warning(f"Could not fetch orders for {user_id}: {e}")
                
            # Derive frequent categories
            freq_cats = []
            if recent_orders:
                cat_counts = {}
                for order in recent_orders:
                    items = order.get("items", [])
                    for item in items:
                        c = item.get("category")
                        if c:
                            cat_counts[c] = cat_counts.get(c, 0) + 1
                freq_cats = [k for k, v in sorted(cat_counts.items(), key=lambda item: item[1], reverse=True)[:3]]

            return UserFoodPreference(
                favoriteCategories=prefs_data.get("favoriteCategories", ["traditional"]),
                favoriteVendors=prefs_data.get("favoriteVendors", []),
                spiceTolerance=prefs_data.get("spiceTolerance", "medium"),
                fastingMode=prefs_data.get("fastingMode", False),
                fastingBehavior=prefs_data.get("fastingBehavior", "None"),
                favoriteMeals=prefs_data.get("favoriteMeals", []),
                dislikedMeals=prefs_data.get("dislikedMeals", []),
                frequentlyOrderedCategories=freq_cats
            )
        except Exception as e:
            logger.error(f"Failed to fetch user preferences from Firestore: {e}")
            return self._get_default_preferences()
