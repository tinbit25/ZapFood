"""
Repositories Package — Data access layer for Firestore collections.

Each repository encapsulates CRUD operations for a specific
Firestore collection, keeping business logic in services clean.
"""

from __future__ import annotations

import logging
import time
import uuid
from typing import Any

from app.services.firebase_client import (
    get_document,
    set_document,
    update_document,
    query_documents,
    now_millis,
)

logger = logging.getLogger(__name__)


class PaymentRepository:
    """
    Data access for the 'payments' Firestore collection.
    Mirrors the Android PaymentRepository interface.
    """

    COLLECTION = "payments"

    async def create_payment(
        self,
        order_id: str,
        user_id: str,
        amount: int,
        method: str = "CARD",
        currency: str = "ETB",
    ) -> dict[str, Any]:
        """
        Create a new payment document in INITIATED status.
        Returns the full payment dict including the generated paymentId.
        """
        payment_id = str(uuid.uuid4())
        now = now_millis()

        payment_data = {
            "paymentId": payment_id,
            "orderId": order_id,
            "userId": user_id,
            "amount": amount,
            "currency": currency,
            "method": method,
            "status": "INITIATED",
            "transactionRef": None,
            "chapaRef": None,
            "checkoutUrl": None,
            "createdAt": now,
            "updatedAt": now,
        }

        success = await set_document(self.COLLECTION, payment_id, payment_data)
        if not success:
            raise RuntimeError(f"Failed to create payment for order {order_id}")

        logger.info(f"Payment {payment_id} created for order {order_id}")
        return payment_data

    async def get_payment_by_id(self, payment_id: str) -> dict[str, Any] | None:
        """Fetch a payment document by its ID."""
        return await get_document(self.COLLECTION, payment_id)

    async def get_payment_by_order_id(self, order_id: str) -> dict[str, Any] | None:
        """Find the most recent payment associated with an order."""
        results = await query_documents(
            self.COLLECTION, "orderId", "==", order_id, limit=1
        )
        return results[0] if results else None

    async def get_payment_by_tx_ref(self, tx_ref: str) -> dict[str, Any] | None:
        """Find a payment by its Chapa transaction reference."""
        results = await query_documents(
            self.COLLECTION, "transactionRef", "==", tx_ref, limit=1
        )
        return results[0] if results else None

    async def update_status(
        self,
        payment_id: str,
        status: str,
        transaction_ref: str | None = None,
        chapa_ref: str | None = None,
        checkout_url: str | None = None,
    ) -> bool:
        """
        Update payment status and optional Chapa references.
        Always updates the updatedAt timestamp.
        """
        updates: dict[str, Any] = {
            "status": status,
            "updatedAt": now_millis(),
        }
        if transaction_ref is not None:
            updates["transactionRef"] = transaction_ref
        if chapa_ref is not None:
            updates["chapaRef"] = chapa_ref
        if checkout_url is not None:
            updates["checkoutUrl"] = checkout_url

        success = await update_document(self.COLLECTION, payment_id, updates)
        if success:
            logger.info(f"Payment {payment_id} status → {status}")
        else:
            logger.error(f"Failed to update payment {payment_id} to {status}")
        return success

    async def is_order_paid(self, order_id: str) -> bool:
        """Check if an order already has a successful payment (duplicate prevention)."""
        payment = await self.get_payment_by_order_id(order_id)
        return payment is not None and payment.get("status") == "SUCCESS"


class OrderRepository:
    """
    Data access for the 'orders' Firestore collection.
    Only exposes payment-related operations needed by the payment backend.
    """

    COLLECTION = "orders"

    async def get_order(self, order_id: str) -> dict[str, Any] | None:
        """Fetch an order document by its ID."""
        order = await get_document(self.COLLECTION, order_id)
        if order is None:
            logger.warning(f"Order {order_id} not found in Firestore")
        return order

    async def update_payment_status(self, order_id: str, status: str) -> bool:
        """
        Update the paymentStatus field on an order.
        Called after payment verification to keep Order and Payment in sync.
        """
        success = await update_document(
            self.COLLECTION,
            order_id,
            {
                "paymentStatus": status,
                "updatedAt": now_millis(),
            },
        )
        if success:
            logger.info(f"Order {order_id} paymentStatus → {status}")
        else:
            logger.error(f"Failed to update order {order_id} paymentStatus to {status}")
        return success

    async def get_order_total(self, order_id: str) -> float | None:
        """Get the total amount for an order. Returns None if order not found."""
        order = await self.get_order(order_id)
        if order is None:
            return None
        return order.get("totalAmount", 0.0)

    async def get_order_customer(self, order_id: str) -> dict[str, str] | None:
        """
        Get customer info from an order for Chapa transaction initialization.
        Returns dict with customerId, customerName, or None.
        """
        order = await self.get_order(order_id)
        if order is None:
            return None
        return {
            "customerId": order.get("customerId", ""),
            "customerName": order.get("customerName", ""),
            "paymentMethod": order.get("paymentMethod", "CARD"),
        }


class UserRepository:
    """
    Data access for the 'users' Firestore collection.
    Used to fetch customer details for Chapa transaction metadata.
    """

    COLLECTION = "users"

    async def get_user(self, user_id: str) -> dict[str, Any] | None:
        """Fetch a user document by Firebase UID."""
        return await get_document(self.COLLECTION, user_id)

    async def get_user_email(self, user_id: str) -> str | None:
        """Get user's email for Chapa transaction."""
        user = await self.get_user(user_id)
        if user is None:
            return None
        return user.get("email")

    async def get_user_phone(self, user_id: str) -> str | None:
        """Get user's phone number for Chapa transaction."""
        user = await self.get_user(user_id)
        if user is None:
            return None
        return user.get("phoneNumber")
