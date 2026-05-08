"""
API Schemas — Pydantic models for structured request/response handling.

These models enforce validation at the API boundary and provide
consistent response shapes across all endpoints.
"""

from __future__ import annotations

import enum
from datetime import datetime
from typing import Any, Generic, TypeVar

from pydantic import BaseModel, Field

T = TypeVar("T")


# ─────────────────────────────────────────────────────────────
# Enums
# ─────────────────────────────────────────────────────────────

class PaymentStatus(str, enum.Enum):
    """Payment lifecycle states — mirrors Android PaymentStatus."""
    INITIATED = "INITIATED"
    PROCESSING = "PROCESSING"
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
    REFUNDED = "REFUNDED"


class PaymentMethod(str, enum.Enum):
    """Supported payment methods."""
    CARD = "CARD"
    MOBILE_MONEY = "MOBILE_MONEY"
    CASH = "CASH"


# ─────────────────────────────────────────────────────────────
# Generic API Response Wrapper
# ─────────────────────────────────────────────────────────────

class ApiResponse(BaseModel, Generic[T]):
    """
    Standardized API response envelope.
    Every endpoint returns this shape for consistency.
    """
    success: bool
    message: str
    data: T | None = None
    timestamp: datetime = Field(default_factory=datetime.utcnow)

    @classmethod
    def ok(cls, data: T | None = None, message: str = "Success") -> "ApiResponse[T]":
        return cls(success=True, message=message, data=data)

    @classmethod
    def error(cls, message: str, data: T | None = None) -> "ApiResponse[T]":
        return cls(success=False, message=message, data=data)


# ─────────────────────────────────────────────────────────────
# Health Check
# ─────────────────────────────────────────────────────────────

class HealthStatus(BaseModel):
    """Response model for the /api/health endpoint."""
    status: str = "healthy"
    version: str = "1.0.0"
    firebase_connected: bool = False
    chapa_configured: bool = False
    currency: str = "ETB"
    debug_mode: bool = False


# ─────────────────────────────────────────────────────────────
# Payment — Initialize
# ─────────────────────────────────────────────────────────────

class InitializePaymentRequest(BaseModel):
    """Request body for POST /api/payments/initialize."""
    order_id: str = Field(..., min_length=1, description="Firestore order document ID")
    user_id: str = Field(..., min_length=1, description="Firebase Auth UID of the payer")


class InitializePaymentResponse(BaseModel):
    """Returned after successfully creating a Chapa transaction."""
    checkout_url: str = Field(..., description="Chapa hosted checkout page URL")
    tx_ref: str = Field(..., description="Unique transaction reference")
    payment_id: str = Field(..., description="Firestore payment document ID")


# ─────────────────────────────────────────────────────────────
# Payment — Verify
# ─────────────────────────────────────────────────────────────

class VerifyPaymentRequest(BaseModel):
    """Query for GET /api/payments/verify/{tx_ref}."""
    tx_ref: str


class VerifyPaymentResponse(BaseModel):
    """Returned after verifying a transaction with Chapa."""
    status: PaymentStatus
    tx_ref: str
    amount: float | None = None
    currency: str = "ETB"
    payment_id: str | None = None
    order_id: str | None = None


# ─────────────────────────────────────────────────────────────
# Payment — Webhook
# ─────────────────────────────────────────────────────────────

class ChapaWebhookPayload(BaseModel):
    """
    Webhook event from Chapa.
    Chapa sends these on payment success/failure.
    """
    trx_ref: str | None = None
    tx_ref: str | None = None
    ref_id: str | None = None
    status: str | None = None
    event: str | None = None

    @property
    def transaction_ref(self) -> str:
        """Chapa may send tx_ref or trx_ref depending on the event."""
        return self.tx_ref or self.trx_ref or ""


# ─────────────────────────────────────────────────────────────
# Payment — Firestore Document Shape
# ─────────────────────────────────────────────────────────────

class PaymentDocument(BaseModel):
    """
    Mirrors the Payment data class in the Android app.
    Used for reading/writing Firestore payment documents.
    """
    paymentId: str = ""
    orderId: str = ""
    userId: str = ""
    amount: int = 0  # In smallest currency unit (cents/santim)
    method: str = "CARD"
    status: str = "INITIATED"
    transactionRef: str | None = None
    chapaRef: str | None = None
    checkoutUrl: str | None = None
    createdAt: int = 0
    updatedAt: int = 0
