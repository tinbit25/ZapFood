"""
Webhook Security — Chapa webhook signature validation and protection.

Chapa signs webhook payloads using your webhook secret.
This module verifies that signature to ensure webhooks
are genuinely from Chapa and haven't been tampered with.
"""

from __future__ import annotations

import hashlib
import hmac
import logging
import time
from typing import Any

from app.config import get_settings

logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────────────────────
# In-memory deduplication cache
# ─────────────────────────────────────────────────────────────
# Tracks recently processed webhook tx_refs to prevent
# rapid-fire duplicate processing before Firestore updates propagate.
# Format: {tx_ref: timestamp_processed}
_processed_webhooks: dict[str, float] = {}
_DEDUP_WINDOW_SECONDS = 300  # 5 minutes


def validate_webhook_signature(
    raw_body: bytes, signature_header: str | None
) -> bool:
    """
    Validate the Chapa webhook signature.

    Chapa sends the webhook secret hash in the request header.
    We compute HMAC-SHA256 of the raw request body using our
    webhook secret and compare.

    Args:
        raw_body: The raw request body bytes
        signature_header: The Chapa-Signature header value

    Returns:
        True if signature is valid or if webhook secret is not configured
        (development mode).
    """
    settings = get_settings()
    webhook_secret = settings.chapa_webhook_secret

    # If no webhook secret configured, allow in dev mode
    if not webhook_secret:
        if settings.backend_debug:
            logger.warning(
                "Webhook secret not configured — accepting webhook in debug mode. "
                "Set CHAPA_WEBHOOK_SECRET in .env for production."
            )
            return True
        else:
            logger.error(
                "Webhook secret not configured in production mode. "
                "Rejecting webhook."
            )
            return False

    # If no signature header provided, reject
    if not signature_header:
        logger.warning("Webhook received without signature header")
        return False

    # Compute expected signature
    expected_signature = hmac.new(
        key=webhook_secret.encode("utf-8"),
        msg=raw_body,
        digestmod=hashlib.sha256,
    ).hexdigest()

    # Constant-time comparison to prevent timing attacks
    is_valid = hmac.compare_digest(expected_signature, signature_header)

    if not is_valid:
        logger.warning(
            f"Webhook signature mismatch. "
            f"Expected: {expected_signature[:16]}..., "
            f"Received: {signature_header[:16]}..."
        )

    return is_valid


def check_dedup(tx_ref: str) -> bool:
    """
    Check if this webhook tx_ref was recently processed.

    Returns True if this is a DUPLICATE (should be skipped).
    Returns False if this is NEW (should be processed).
    """
    now = time.time()

    # Clean old entries
    expired = [
        ref for ref, ts in _processed_webhooks.items()
        if now - ts > _DEDUP_WINDOW_SECONDS
    ]
    for ref in expired:
        del _processed_webhooks[ref]

    # Check for duplicate
    if tx_ref in _processed_webhooks:
        elapsed = now - _processed_webhooks[tx_ref]
        logger.info(
            f"Duplicate webhook detected: tx_ref={tx_ref}, "
            f"last processed {elapsed:.1f}s ago"
        )
        return True  # Is duplicate

    return False  # Not duplicate


def mark_processed(tx_ref: str) -> None:
    """Mark a webhook tx_ref as processed in the dedup cache."""
    _processed_webhooks[tx_ref] = time.time()


def get_webhook_stats() -> dict[str, Any]:
    """Return webhook processing statistics (for health/debug)."""
    now = time.time()
    active = {
        ref: now - ts
        for ref, ts in _processed_webhooks.items()
        if now - ts <= _DEDUP_WINDOW_SECONDS
    }
    return {
        "active_dedup_entries": len(active),
        "dedup_window_seconds": _DEDUP_WINDOW_SECONDS,
    }
