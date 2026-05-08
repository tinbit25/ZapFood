"""
Firebase Client — Firebase Admin SDK initialization and Firestore helpers.

This module initializes the Firebase Admin SDK once at startup and
provides async-compatible helpers for reading/writing Firestore documents.
All Firestore operations run through this single module to ensure
consistent error handling and connection management.
"""

from __future__ import annotations

import logging
import os
import time
from typing import Any

import firebase_admin
from firebase_admin import credentials, firestore
from google.cloud.firestore_v1.base_query import FieldFilter

from app.config import get_settings

logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────────────────────
# Module-level state
# ─────────────────────────────────────────────────────────────
_firestore_client: firestore.firestore.Client | None = None
_initialized: bool = False


def initialize_firebase() -> bool:
    """
    Initialize the Firebase Admin SDK.

    Returns True if Firebase was successfully initialized, False otherwise.
    Safe to call multiple times — will no-op if already initialized.
    """
    global _firestore_client, _initialized

    if _initialized:
        logger.info("Firebase already initialized, skipping.")
        return True

    settings = get_settings()
    cred_path = settings.firebase_credentials_path

    try:
        if not os.path.exists(cred_path):
            logger.warning(
                f"Firebase credentials file not found at '{cred_path}'. "
                "Firestore operations will not work. "
                "Download from Firebase Console → Project Settings → Service Accounts."
            )
            _initialized = False
            return False

        cred = credentials.Certificate(cred_path)

        # Avoid re-initializing if the default app already exists
        if not firebase_admin._apps:
            firebase_admin.initialize_app(cred)
            logger.info("Firebase Admin SDK initialized successfully.")
        else:
            logger.info("Firebase Admin SDK already has a default app.")

        _firestore_client = firestore.client()
        _initialized = True

        # Quick connectivity test — try to access a collection
        try:
            _firestore_client.collection("_health_check").limit(1).get()
            logger.info("Firestore connectivity verified.")
        except Exception as e:
            logger.warning(f"Firestore connectivity check failed: {e}")

        return True

    except Exception as e:
        logger.error(f"Failed to initialize Firebase: {e}")
        _initialized = False
        return False


def get_firestore_client() -> firestore.firestore.Client:
    """Get the Firestore client. Raises RuntimeError if not initialized."""
    if _firestore_client is None:
        raise RuntimeError(
            "Firestore client is not initialized. "
            "Ensure Firebase credentials are configured."
        )
    return _firestore_client


def is_firebase_connected() -> bool:
    """Check if Firebase has been successfully initialized."""
    return _initialized


# ─────────────────────────────────────────────────────────────
# Firestore CRUD Helpers
# ─────────────────────────────────────────────────────────────

def _get_collection(collection_name: str):
    """Get a Firestore collection reference."""
    return get_firestore_client().collection(collection_name)


async def get_document(collection_name: str, doc_id: str) -> dict[str, Any] | None:
    """
    Fetch a single document from Firestore by ID.
    Returns the document data as a dict, or None if not found.
    """
    try:
        doc = _get_collection(collection_name).document(doc_id).get()
        if doc.exists:
            return doc.to_dict()
        return None
    except Exception as e:
        logger.error(f"Error fetching {collection_name}/{doc_id}: {e}")
        return None


async def set_document(
    collection_name: str, doc_id: str, data: dict[str, Any]
) -> bool:
    """
    Create or overwrite a document in Firestore.
    Returns True on success.
    """
    try:
        _get_collection(collection_name).document(doc_id).set(data)
        return True
    except Exception as e:
        logger.error(f"Error writing {collection_name}/{doc_id}: {e}")
        return False


async def update_document(
    collection_name: str, doc_id: str, updates: dict[str, Any]
) -> bool:
    """
    Partially update fields on an existing document.
    Returns True on success.
    """
    try:
        _get_collection(collection_name).document(doc_id).update(updates)
        return True
    except Exception as e:
        logger.error(f"Error updating {collection_name}/{doc_id}: {e}")
        return False


async def query_documents(
    collection_name: str,
    field: str,
    operator: str,
    value: Any,
    limit: int = 10,
) -> list[dict[str, Any]]:
    """
    Query documents from a Firestore collection with a single filter.
    Returns a list of document dicts.
    """
    try:
        query = (
            _get_collection(collection_name)
            .where(filter=FieldFilter(field, operator, value))
            .limit(limit)
        )
        docs = query.get()
        return [doc.to_dict() for doc in docs if doc.exists]
    except Exception as e:
        logger.error(f"Error querying {collection_name}: {e}")
        return []


async def run_transaction(callback) -> Any:
    """
    Execute a Firestore transaction for atomic reads and writes.

    The callback receives the transaction object and should perform
    all reads and writes within it.

    Usage:
        async def my_transaction(transaction):
            doc = collection.document(doc_id).get(transaction=transaction)
            transaction.update(doc.reference, {"field": "value"})
            return doc

        result = await run_transaction(my_transaction)
    """
    try:
        client = get_firestore_client()
        transaction = client.transaction()

        @firestore.transactional
        def _in_transaction(transaction):
            return callback(transaction)

        return _in_transaction(transaction)
    except Exception as e:
        logger.error(f"Transaction failed: {e}")
        raise


# ─────────────────────────────────────────────────────────────
# Convenience: Timestamp helper
# ─────────────────────────────────────────────────────────────

def now_millis() -> int:
    """Current time in milliseconds (matches Android's System.currentTimeMillis())."""
    return int(time.time() * 1000)
