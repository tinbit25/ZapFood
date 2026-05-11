import logging
from typing import List, Optional
from app.models.behavior import BehaviorEvent
from app.config import get_settings
from app.services.firebase_client import get_firestore_client, is_firebase_connected

logger = logging.getLogger("behavior-repo")

class BehaviorRepository:
    def __init__(self):
        # Use the Firebase Admin SDK client — never google.cloud.firestore.Client() directly
        self.settings = get_settings()
        self._collection_name = self.settings.behavior_collection

    @property
    def collection(self):
        """Lazily get the Firestore collection, so initialization order doesn't matter."""
        return get_firestore_client().collection(self._collection_name)

    def add_event(self, event: BehaviorEvent):
        if not is_firebase_connected():
            logger.warning("Firebase not connected — behavior event dropped")
            return False
        try:
            doc_ref = self.collection.document(event.id)
            doc_ref.set(event.model_dump())
            return True
        except Exception as e:
            logger.error(f"Failed to add behavior event: {e}")
            return False

    def get_user_behavior(self, user_id: str, limit: int = 100) -> List[dict]:
        if not is_firebase_connected():
            return []
        try:
            from google.cloud.firestore_v1.base_query import FieldFilter
            docs = (
                self.collection
                .where(filter=FieldFilter("userId", "==", user_id))
                .order_by("timestamp", direction="DESCENDING")
                .limit(limit)
                .stream()
            )
            return [doc.to_dict() for doc in docs]
        except Exception as e:
            logger.error(f"Failed to fetch user behavior: {e}")
            return []

    def get_all_behavior(self, limit: int = 1000) -> List[dict]:
        if not is_firebase_connected():
            return []
        try:
            docs = (
                self.collection
                .order_by("timestamp", direction="DESCENDING")
                .limit(limit)
                .stream()
            )
            return [doc.to_dict() for doc in docs]
        except Exception as e:
            logger.error(f"Failed to fetch all behavior: {e}")
            return []
