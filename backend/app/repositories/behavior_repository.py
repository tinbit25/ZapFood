import logging
from typing import List, Optional
from google.cloud import firestore
from app.models.behavior import BehaviorEvent
from app.config import get_settings

logger = logging.getLogger("behavior-repo")

class BehaviorRepository:
    def __init__(self):
        self.db = firestore.Client()
        self.settings = get_settings()
        self.collection = self.db.collection(self.settings.behavior_collection)

    def add_event(self, event: BehaviorEvent):
        try:
            doc_ref = self.collection.document(event.id)
            doc_ref.set(event.model_dump())
            return True
        except Exception as e:
            logger.error(f"Failed to add behavior event: {e}")
            return False

    def get_user_behavior(self, user_id: str, limit: int = 100) -> List[dict]:
        try:
            docs = self.collection.where("userId", "==", user_id)\
                        .order_by("timestamp", direction=firestore.Query.DESCENDING)\
                        .limit(limit).stream()
            return [doc.to_dict() for doc in docs]
        except Exception as e:
            logger.error(f"Failed to fetch user behavior: {e}")
            return []

    def get_all_behavior(self, limit: int = 1000) -> List[dict]:
        try:
            docs = self.collection.order_by("timestamp", direction=firestore.Query.DESCENDING)\
                        .limit(limit).stream()
            return [doc.to_dict() for doc in docs]
        except Exception as e:
            logger.error(f"Failed to fetch all behavior: {e}")
            return []
