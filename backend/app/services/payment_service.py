"""
Payment Service — Core business logic for Chapa payment processing.

This module orchestrates the full payment lifecycle:
1. Initialize: Validate order → prevent duplicates → create record → call Chapa → return checkout URL
2. Verify: Call Chapa verify → update Payment status → update Order paymentStatus
3. Webhook: Validate → idempotency check → verify with Chapa → sync Firestore

All Chapa communication goes through ChapaClient.
All Firestore operations go through Repositories.
"""

from __future__ import annotations

import logging
from typing import Any

from app.config import get_settings
from app.models.schemas import (
    PaymentStatus,
    InitializePaymentResponse,
    VerifyPaymentResponse,
)
from app.repositories import PaymentRepository, OrderRepository, UserRepository
from app.services.chapa_client import get_chapa_client, ChapaInitResult, ChapaVerifyResult

logger = logging.getLogger(__name__)


class PaymentServiceError(Exception):
    """Raised when a payment operation fails due to business rule violation."""
    pass


class PaymentService:
    """
    Orchestrates the payment lifecycle.

    Usage:
        service = PaymentService()
        result = await service.initialize_payment(order_id, user_id)
        verify = await service.verify_payment(tx_ref)
    """

    def __init__(self) -> None:
        self.payment_repo = PaymentRepository()
        self.order_repo = OrderRepository()
        self.user_repo = UserRepository()
        self.chapa = get_chapa_client()
        self.settings = get_settings()

    # ─────────────────────────────────────────────────────────
    # 1. Initialize Payment
    # ─────────────────────────────────────────────────────────

    async def initialize_payment(
        self, order_id: str, user_id: str
    ) -> InitializePaymentResponse:
        """
        Full payment initialization flow:

        1. Validate the order exists and belongs to the user
        2. Prevent duplicate successful payments
        3. Create a Payment record in Firestore (INITIATED)
        4. Fetch user details for Chapa (email, phone, name)
        5. Call Chapa initialize API
        6. Update Payment to PROCESSING with tx_ref + checkout URL
        7. Return checkout URL to the Android app

        Raises PaymentServiceError on business rule violations.
        """
        logger.info(f"Initializing payment: order={order_id}, user={user_id}")

        # ── Step 1: Validate Order ────────────────────────────
        order = await self.order_repo.get_order(order_id)
        if order is None:
            raise PaymentServiceError(f"Order not found: {order_id}")

        if order.get("customerId") != user_id:
            raise PaymentServiceError(
                "Unauthorized: You do not own this order"
            )

        # ── Step 2: Prevent Duplicate Payments ────────────────
        if await self.payment_repo.is_order_paid(order_id):
            raise PaymentServiceError(
                "Order already paid. Duplicate payment prevented."
            )

        # Check for an existing PROCESSING payment (user may have abandoned checkout)
        existing = await self.payment_repo.get_payment_by_order_id(order_id)
        if existing and existing.get("status") == "PROCESSING":
            # Return the existing checkout URL if still valid
            checkout_url = existing.get("checkoutUrl")
            tx_ref = existing.get("transactionRef")
            if checkout_url and tx_ref:
                logger.info(
                    f"Returning existing PROCESSING payment: {existing['paymentId']}"
                )
                return InitializePaymentResponse(
                    checkout_url=checkout_url,
                    tx_ref=tx_ref,
                    payment_id=existing["paymentId"],
                )

        # ── Step 3: Create Payment Record ─────────────────────
        total_amount = order.get("totalAmount", 0.0)
        payment_method = order.get("paymentMethod", "CARD")

        payment = await self.payment_repo.create_payment(
            order_id=order_id,
            user_id=user_id,
            amount=int(total_amount * 1000),  # Convert to smallest unit (santim)
            method=payment_method,
            currency=self.settings.default_currency,
        )
        payment_id = payment["paymentId"]
        logger.info(f"Payment record created: {payment_id}")

        # ── Step 4: Fetch User Details ────────────────────────
        user = await self.user_repo.get_user(user_id)
        email = "customer@zapfood.com"
        first_name = "Customer"
        last_name = ""
        phone_number = None

        if user:
            email = user.get("email", email)
            display_name = user.get("displayName", "Customer")
            name_parts = display_name.split(" ", 1)
            first_name = name_parts[0]
            last_name = name_parts[1] if len(name_parts) > 1 else ""
            phone_number = user.get("phoneNumber") or None

        # ── Step 5: Call Chapa Initialize ─────────────────────
        callback_url = f"{self.settings.backend_base_url}/api/payments/webhook"
        return_url = f"{self.settings.backend_base_url}/api/payments/return"

        chapa_result: ChapaInitResult = await self.chapa.initialize_transaction(
            amount=total_amount,
            email=email,
            first_name=first_name,
            last_name=last_name,
            phone_number=phone_number,
            callback_url=callback_url,
            return_url=return_url,
            customization_title="ZapFood",
            customization_description=f"Order {order_id[:8]}",
            meta={"order_id": order_id, "payment_id": payment_id},
        )

        if not chapa_result.success or not chapa_result.checkout_url:
            # Chapa failed — mark payment as FAILED
            await self.payment_repo.update_status(payment_id, "FAILED")
            raise PaymentServiceError(
                f"Chapa initialization failed: {chapa_result.message}"
            )

        # ── Step 6: Update Payment to PROCESSING ──────────────
        await self.payment_repo.update_status(
            payment_id=payment_id,
            status="PROCESSING",
            transaction_ref=chapa_result.tx_ref,
            checkout_url=chapa_result.checkout_url,
        )

        # Also update order paymentStatus to PROCESSING
        await self.order_repo.update_payment_status(order_id, "PROCESSING")

        logger.info(
            f"Payment initialized: payment={payment_id}, "
            f"tx_ref={chapa_result.tx_ref}, "
            f"checkout={chapa_result.checkout_url}"
        )

        # ── Step 7: Return Response ───────────────────────────
        return InitializePaymentResponse(
            checkout_url=chapa_result.checkout_url,
            tx_ref=chapa_result.tx_ref or "",
            payment_id=payment_id,
        )

    # ─────────────────────────────────────────────────────────
    # 2. Verify Payment
    # ─────────────────────────────────────────────────────────

    async def verify_payment(self, tx_ref: str) -> VerifyPaymentResponse:
        """
        Verify a payment transaction with Chapa and sync Firestore.

        1. Find the payment record by tx_ref
        2. Call Chapa verify API
        3. Map Chapa status → PaymentStatus
        4. Update Payment document
        5. Update Order paymentStatus
        6. Return verification result

        Idempotent: if already SUCCESS, returns current status without re-verifying.
        """
        logger.info(f"Verifying payment: tx_ref={tx_ref}")

        # ── Find Payment Record ───────────────────────────────
        payment = await self.payment_repo.get_payment_by_tx_ref(tx_ref)

        # ── Idempotency: Already finalized? ───────────────────
        if payment and payment.get("status") in ("SUCCESS", "REFUNDED"):
            logger.info(
                f"Payment already finalized: {payment['paymentId']} "
                f"status={payment['status']}"
            )
            return VerifyPaymentResponse(
                status=PaymentStatus(payment["status"]),
                tx_ref=tx_ref,
                amount=float(payment.get("amount", 0)) / 1000,
                currency=payment.get("currency", "ETB"),
                payment_id=payment.get("paymentId"),
                order_id=payment.get("orderId"),
            )

        # ── Call Chapa Verify ─────────────────────────────────
        chapa_result: ChapaVerifyResult = await self.chapa.verify_transaction(tx_ref)

        if not chapa_result.success:
            logger.warning(f"Chapa verify failed: {chapa_result.message}")
            return VerifyPaymentResponse(
                status=PaymentStatus.FAILED,
                tx_ref=tx_ref,
                payment_id=payment.get("paymentId") if payment else None,
                order_id=payment.get("orderId") if payment else None,
            )

        # ── Map Chapa Status → PaymentStatus ──────────────────
        new_status = self._map_chapa_status(chapa_result.status)

        # ── Update Firestore ──────────────────────────────────
        if payment:
            payment_id = payment["paymentId"]
            order_id = payment.get("orderId", "")

            await self.payment_repo.update_status(
                payment_id=payment_id,
                status=new_status.value,
                chapa_ref=chapa_result.ref_id or None,
            )

            if order_id:
                await self.order_repo.update_payment_status(
                    order_id, new_status.value
                )

            logger.info(
                f"Payment verified: payment={payment_id}, "
                f"chapa_status={chapa_result.status}, "
                f"mapped_status={new_status.value}"
            )
        else:
            payment_id = None
            order_id = None
            logger.warning(
                f"No payment record found for tx_ref={tx_ref}, "
                f"Chapa reports status={chapa_result.status}"
            )

        return VerifyPaymentResponse(
            status=new_status,
            tx_ref=tx_ref,
            amount=chapa_result.amount,
            currency=chapa_result.currency,
            payment_id=payment_id,
            order_id=order_id,
        )

    # ─────────────────────────────────────────────────────────
    # 3. Handle Webhook
    # ─────────────────────────────────────────────────────────

    async def handle_webhook(
        self, tx_ref: str, status: str, ref_id: str | None = None
    ) -> dict[str, Any]:
        """
        Process a Chapa webhook callback securely.

        Security layers:
        1. Signature validation (done in the route layer)
        2. In-memory dedup cache (prevents rapid-fire duplicates)
        3. Firestore idempotency check (prevents stale duplicates)
        4. Chapa API re-verification (never trust webhook data alone)
        5. Amount validation (ensures amount matches order)
        6. Atomic Firestore updates (Payment + Order in sync)

        Returns a dict with processing details for audit logging.
        """
        from app.services.webhook_security import check_dedup, mark_processed

        logger.info(
            f"WEBHOOK PROCESSING START: tx_ref={tx_ref}, "
            f"reported_status={status}, ref_id={ref_id}"
        )

        result = {
            "tx_ref": tx_ref,
            "action": "none",
            "reason": "",
            "final_status": None,
        }

        # ── Layer 2: In-Memory Dedup ──────────────────────────
        if check_dedup(tx_ref):
            result["action"] = "skipped"
            result["reason"] = "duplicate_in_dedup_cache"
            logger.info(f"WEBHOOK SKIPPED (dedup cache): tx_ref={tx_ref}")
            return result

        # ── Layer 3: Firestore Idempotency ────────────────────
        payment = await self.payment_repo.get_payment_by_tx_ref(tx_ref)
        if payment is None:
            result["action"] = "skipped"
            result["reason"] = "payment_not_found"
            logger.warning(f"WEBHOOK SKIPPED (no payment): tx_ref={tx_ref}")
            return result

        payment_id = payment["paymentId"]
        order_id = payment.get("orderId", "")
        current_status = payment.get("status", "")

        if current_status in ("SUCCESS", "REFUNDED"):
            mark_processed(tx_ref)
            result["action"] = "skipped"
            result["reason"] = f"already_{current_status.lower()}"
            result["final_status"] = current_status
            logger.info(
                f"WEBHOOK SKIPPED (already finalized): tx_ref={tx_ref}, "
                f"payment={payment_id}, status={current_status}"
            )
            return result

        # ── Layer 4: Chapa API Re-Verification ────────────────
        chapa_result = await self.chapa.verify_transaction(tx_ref)

        if not chapa_result.success:
            result["action"] = "verification_failed"
            result["reason"] = chapa_result.message
            logger.error(
                f"WEBHOOK VERIFY FAILED: tx_ref={tx_ref}, "
                f"message={chapa_result.message}"
            )
            return result

        new_status = self._map_chapa_status(chapa_result.status)

        # ── Layer 5: Amount Validation ────────────────────────
        if new_status == PaymentStatus.SUCCESS and order_id:
            order = await self.order_repo.get_order(order_id)
            if order:
                expected_amount = order.get("totalAmount", 0.0)
                received_amount = chapa_result.amount
                # Allow small floating-point tolerance
                if abs(expected_amount - received_amount) > 1.0:
                    logger.error(
                        f"WEBHOOK AMOUNT MISMATCH: tx_ref={tx_ref}, "
                        f"expected={expected_amount}, received={received_amount}. "
                        f"Marking as FAILED for manual review."
                    )
                    new_status = PaymentStatus.FAILED
                    result["reason"] = "amount_mismatch"

        # ── Layer 6: Atomic Firestore Updates ─────────────────
        await self.payment_repo.update_status(
            payment_id=payment_id,
            status=new_status.value,
            chapa_ref=chapa_result.ref_id or ref_id or None,
        )

        if order_id:
            await self.order_repo.update_payment_status(
                order_id, new_status.value
            )

        # Mark as processed in dedup cache
        mark_processed(tx_ref)

        result["action"] = "processed"
        result["final_status"] = new_status.value
        logger.info(
            f"WEBHOOK PROCESSED: tx_ref={tx_ref}, payment={payment_id}, "
            f"chapa_status={chapa_result.status} -> {new_status.value}"
        )
        return result

    # ─────────────────────────────────────────────────────────
    # Helpers
    # ─────────────────────────────────────────────────────────

    @staticmethod
    def _map_chapa_status(chapa_status: str) -> PaymentStatus:
        """Map Chapa's status string to our PaymentStatus enum."""
        status_lower = chapa_status.lower().strip()
        mapping = {
            "success": PaymentStatus.SUCCESS,
            "successful": PaymentStatus.SUCCESS,
            "passed": PaymentStatus.SUCCESS,
            "failed": PaymentStatus.FAILED,
            "fail": PaymentStatus.FAILED,
            "error": PaymentStatus.FAILED,
            "pending": PaymentStatus.PROCESSING,
            "processing": PaymentStatus.PROCESSING,
        }
        return mapping.get(status_lower, PaymentStatus.FAILED)


# ─────────────────────────────────────────────────────────────
# Module-level singleton
# ─────────────────────────────────────────────────────────────

_service: PaymentService | None = None


def get_payment_service() -> PaymentService:
    """Get or create the PaymentService singleton."""
    global _service
    if _service is None:
        _service = PaymentService()
    return _service
