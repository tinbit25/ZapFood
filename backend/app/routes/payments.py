"""
Payment Routes — Live Chapa payment endpoints with webhook security.

All endpoints use the PaymentService for business logic
and return structured ApiResponse envelopes.
"""

from fastapi import APIRouter, Request, Header

from app.models.schemas import (
    ApiResponse,
    InitializePaymentRequest,
    InitializePaymentResponse,
    VerifyPaymentResponse,
)
from app.services.payment_service import get_payment_service, PaymentServiceError
from app.services.webhook_security import (
    validate_webhook_signature,
    get_webhook_stats,
)

router = APIRouter(prefix="/payments", tags=["Payments"])


@router.post(
    "/initialize",
    response_model=ApiResponse[InitializePaymentResponse],
    summary="Initialize Payment",
    description="Create a Chapa transaction for an order. Returns a checkout URL.",
)
async def initialize_payment(
    request: InitializePaymentRequest,
) -> ApiResponse[InitializePaymentResponse]:
    """
    Payment initialization flow:
    1. Validate order exists and belongs to user
    2. Prevent duplicate successful payments
    3. Create Payment record in Firestore
    4. Call Chapa initialize API
    5. Return checkout URL + transaction reference
    """
    try:
        service = get_payment_service()
        result = await service.initialize_payment(
            order_id=request.order_id,
            user_id=request.user_id,
        )
        return ApiResponse.ok(
            data=result,
            message="Payment initialized. Redirect user to checkout URL.",
        )
    except PaymentServiceError as e:
        return ApiResponse.error(message=str(e))
    except Exception as e:
        return ApiResponse.error(message=f"Internal error: {str(e)}")


@router.get(
    "/verify/{tx_ref}",
    response_model=ApiResponse[VerifyPaymentResponse],
    summary="Verify Payment",
    description="Verify a payment transaction status via its reference.",
)
async def verify_payment(tx_ref: str) -> ApiResponse[VerifyPaymentResponse]:
    """
    Payment verification flow:
    1. Call Chapa verify API with tx_ref
    2. Update Payment status in Firestore
    3. Update Order paymentStatus in Firestore
    4. Return final status
    """
    try:
        service = get_payment_service()
        result = await service.verify_payment(tx_ref)
        return ApiResponse.ok(
            data=result,
            message=f"Payment status: {result.status.value}",
        )
    except Exception as e:
        return ApiResponse.error(message=f"Verification error: {str(e)}")


@router.post(
    "/webhook",
    response_model=ApiResponse[None],
    summary="Chapa Webhook",
    description="Receives payment event notifications from Chapa.",
)
async def chapa_webhook(
    request: Request,
    chapa_signature: str | None = Header(None, alias="Chapa-Signature"),
) -> ApiResponse[None]:
    """
    Secure webhook handler with 6 protection layers:
    1. Signature validation (HMAC-SHA256)
    2. In-memory dedup cache (5-minute window)
    3. Firestore idempotency (skip finalized payments)
    4. Chapa API re-verification (never trust webhook data)
    5. Amount validation (match against order)
    6. Atomic Firestore sync (Payment + Order updated together)
    """
    # ── Layer 1: Signature Validation ─────────────────────
    raw_body = await request.body()

    if not validate_webhook_signature(raw_body, chapa_signature):
        return ApiResponse.error(message="Invalid webhook signature")

    # ── Parse Payload ─────────────────────────────────────
    import json
    try:
        payload = json.loads(raw_body)
    except json.JSONDecodeError:
        return ApiResponse.error(message="Invalid JSON payload")

    tx_ref = payload.get("tx_ref") or payload.get("trx_ref") or ""
    status = payload.get("status") or ""
    ref_id = payload.get("ref_id")

    if not tx_ref:
        return ApiResponse.error(message="Missing transaction reference in webhook")

    # ── Process Webhook ───────────────────────────────────
    try:
        service = get_payment_service()
        result = await service.handle_webhook(
            tx_ref=tx_ref,
            status=status,
            ref_id=ref_id,
        )

        action = result.get("action", "unknown")
        if action == "processed":
            return ApiResponse.ok(
                message=f"Webhook processed: {result.get('final_status', 'unknown')}"
            )
        elif action == "skipped":
            return ApiResponse.ok(
                message=f"Webhook skipped: {result.get('reason', 'unknown')}"
            )
        else:
            return ApiResponse.error(
                message=f"Webhook issue: {result.get('reason', 'unknown')}"
            )
    except Exception as e:
        return ApiResponse.error(message=f"Webhook processing error: {str(e)}")


@router.get(
    "/webhook/stats",
    response_model=ApiResponse[dict],
    summary="Webhook Stats",
    description="Debug endpoint showing webhook dedup cache stats.",
)
async def webhook_stats() -> ApiResponse[dict]:
    """Returns webhook processing statistics."""
    return ApiResponse.ok(data=get_webhook_stats(), message="Webhook stats")


@router.get(
    "/return",
    include_in_schema=False,
    summary="Payment Return",
)
async def payment_return(request: Request):
    """
    Chapa redirects the user here after payment.
    In production, this would redirect to a deep link (food://payment/complete).
    """
    return {
        "message": "Payment complete. You can return to the app.",
        "status": "return",
        "params": dict(request.query_params),
    }
