"""
Chapa API Client — Async HTTP wrapper for Chapa payment gateway.

This module handles ALL direct communication with the Chapa API.
No other module should call Chapa endpoints directly.

API Reference: https://developer.chapa.co/docs/accept-payments/
Base URL: https://api.chapa.co/v1

Endpoints used:
- POST /transaction/initialize  → Create a new transaction
- GET  /transaction/verify/{tx_ref} → Verify transaction status
"""

from __future__ import annotations

import logging
import uuid
from dataclasses import dataclass
from typing import Any

import httpx

from app.config import get_settings

logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────────────────────
# Response Types
# ─────────────────────────────────────────────────────────────

@dataclass
class ChapaInitResult:
    """Result of a transaction initialization call."""
    success: bool
    checkout_url: str | None = None
    tx_ref: str | None = None
    message: str = ""
    raw_response: dict[str, Any] | None = None


@dataclass
class ChapaVerifyResult:
    """Result of a transaction verification call."""
    success: bool
    status: str = ""  # "success", "failed", "pending"
    tx_ref: str = ""
    ref_id: str = ""
    amount: float = 0.0
    currency: str = "ETB"
    first_name: str = ""
    last_name: str = ""
    email: str = ""
    message: str = ""
    raw_response: dict[str, Any] | None = None


# ─────────────────────────────────────────────────────────────
# Chapa Client
# ─────────────────────────────────────────────────────────────

class ChapaClient:
    """
    Async HTTP client for the Chapa payment gateway.

    Usage:
        client = ChapaClient()
        result = await client.initialize_transaction(...)
        verify = await client.verify_transaction(tx_ref)
    """

    # Retry configuration
    MAX_RETRIES = 3
    RETRY_DELAY_SECONDS = 1.0
    TIMEOUT_SECONDS = 30.0

    def __init__(self) -> None:
        settings = get_settings()
        self._base_url = settings.chapa_base_url.rstrip("/")
        self._secret_key = settings.chapa_secret_key
        self._headers = {
            "Authorization": f"Bearer {self._secret_key}",
            "Content-Type": "application/json",
        }

    # ── Public API ───────────────────────────────────────────

    async def initialize_transaction(
        self,
        amount: float,
        email: str,
        first_name: str,
        last_name: str,
        tx_ref: str | None = None,
        phone_number: str | None = None,
        callback_url: str | None = None,
        return_url: str | None = None,
        customization_title: str = "ZapFood",
        customization_description: str = "Food order",
        meta: dict[str, Any] | None = None,
    ) -> ChapaInitResult:
        """
        Initialize a Chapa transaction and get a checkout URL.

        POST https://api.chapa.co/v1/transaction/initialize

        Args:
            amount: Payment amount in ETB
            email: Customer email address
            first_name: Customer first name
            last_name: Customer last name
            tx_ref: Unique transaction reference (auto-generated if None)
            phone_number: Customer phone (format: 09xxxxxxxx)
            callback_url: URL Chapa will call after payment (webhook)
            return_url: URL to redirect user after payment
            customization_title: Title shown on checkout page
            customization_description: Description on checkout page
            meta: Additional metadata to attach to the transaction

        Returns:
            ChapaInitResult with checkout_url on success
        """
        if tx_ref is None:
            tx_ref = self._generate_tx_ref()

        payload: dict[str, Any] = {
            "amount": str(amount),
            "currency": "ETB",
            "email": email,
            "first_name": first_name,
            "last_name": last_name,
            "tx_ref": tx_ref,
            "customization": {
                "title": customization_title,
                "description": customization_description,
            },
        }

        # Optional fields — only include if provided
        if phone_number:
            payload["phone_number"] = phone_number
        if callback_url:
            payload["callback_url"] = callback_url
        if return_url:
            payload["return_url"] = return_url
        if meta:
            payload["meta"] = meta

        logger.info(
            f"Initializing Chapa transaction: tx_ref={tx_ref}, "
            f"amount={amount} ETB, email={email}"
        )

        response = await self._post("/transaction/initialize", payload)

        if response is None:
            return ChapaInitResult(
                success=False,
                tx_ref=tx_ref,
                message="Failed to connect to Chapa API",
            )

        # Parse Chapa's response
        status = response.get("status")
        message = response.get("message", "")
        data = response.get("data", {})

        if status == "success" and data.get("checkout_url"):
            checkout_url = data["checkout_url"]
            logger.info(
                f"Chapa transaction initialized: tx_ref={tx_ref}, "
                f"checkout_url={checkout_url}"
            )
            return ChapaInitResult(
                success=True,
                checkout_url=checkout_url,
                tx_ref=tx_ref,
                message=message,
                raw_response=response,
            )
        else:
            error_msg = message or "Unknown error from Chapa"
            logger.error(
                f"Chapa initialization failed: tx_ref={tx_ref}, "
                f"message={error_msg}, response={response}"
            )
            return ChapaInitResult(
                success=False,
                tx_ref=tx_ref,
                message=error_msg,
                raw_response=response,
            )

    async def verify_transaction(self, tx_ref: str) -> ChapaVerifyResult:
        """
        Verify a transaction status using its reference.

        GET https://api.chapa.co/v1/transaction/verify/{tx_ref}

        Args:
            tx_ref: The transaction reference used during initialization

        Returns:
            ChapaVerifyResult with status and transaction details
        """
        logger.info(f"Verifying Chapa transaction: tx_ref={tx_ref}")

        response = await self._get(f"/transaction/verify/{tx_ref}")

        if response is None:
            return ChapaVerifyResult(
                success=False,
                tx_ref=tx_ref,
                message="Failed to connect to Chapa API",
            )

        status = response.get("status")
        message = response.get("message", "")
        data = response.get("data", {})

        if status == "success" and data:
            payment_status = str(data.get("status", "")).lower()
            ref_id = data.get("reference", data.get("ref_id", ""))

            logger.info(
                f"Chapa verification result: tx_ref={tx_ref}, "
                f"status={payment_status}, ref_id={ref_id}"
            )

            return ChapaVerifyResult(
                success=True,
                status=payment_status,
                tx_ref=tx_ref,
                ref_id=str(ref_id),
                amount=float(data.get("amount", 0)),
                currency=data.get("currency", "ETB"),
                first_name=data.get("first_name", ""),
                last_name=data.get("last_name", ""),
                email=data.get("email", ""),
                message=message,
                raw_response=response,
            )
        else:
            error_msg = message or "Verification failed"
            logger.error(
                f"Chapa verification failed: tx_ref={tx_ref}, "
                f"message={error_msg}"
            )
            return ChapaVerifyResult(
                success=False,
                tx_ref=tx_ref,
                message=error_msg,
                raw_response=response,
            )

    # ── Internal HTTP Methods ────────────────────────────────

    async def _post(
        self, path: str, payload: dict[str, Any]
    ) -> dict[str, Any] | None:
        """POST request with retry and timeout handling."""
        url = f"{self._base_url}{path}"
        return await self._request("POST", url, json_data=payload)

    async def _get(self, path: str) -> dict[str, Any] | None:
        """GET request with retry and timeout handling."""
        url = f"{self._base_url}{path}"
        return await self._request("GET", url)

    async def _request(
        self,
        method: str,
        url: str,
        json_data: dict[str, Any] | None = None,
    ) -> dict[str, Any] | None:
        """
        Execute an HTTP request with retry logic, timeout, and error parsing.

        Retries on:
        - Network errors (ConnectError, TimeoutException)
        - 5xx server errors
        - 429 rate limiting

        Does NOT retry on:
        - 4xx client errors (except 429)
        - Successful responses
        """
        last_error: str = ""

        for attempt in range(1, self.MAX_RETRIES + 1):
            try:
                async with httpx.AsyncClient(
                    timeout=httpx.Timeout(self.TIMEOUT_SECONDS)
                ) as client:
                    if method == "POST":
                        response = await client.post(
                            url, json=json_data, headers=self._headers
                        )
                    else:
                        response = await client.get(url, headers=self._headers)

                    # Log the raw response for debugging
                    logger.debug(
                        f"Chapa {method} {url} → {response.status_code}: "
                        f"{response.text[:200]}"
                    )

                    # Success range
                    if 200 <= response.status_code < 300:
                        return response.json()

                    # Rate limited — retry
                    if response.status_code == 429:
                        last_error = "Rate limited by Chapa"
                        logger.warning(
                            f"Chapa rate limit hit (attempt {attempt}/{self.MAX_RETRIES})"
                        )
                        await self._wait_before_retry(attempt)
                        continue

                    # Server error — retry
                    if response.status_code >= 500:
                        last_error = f"Chapa server error: {response.status_code}"
                        logger.warning(
                            f"Chapa server error {response.status_code} "
                            f"(attempt {attempt}/{self.MAX_RETRIES})"
                        )
                        await self._wait_before_retry(attempt)
                        continue

                    # Client error (4xx) — do NOT retry, parse error
                    error_body = self._parse_error_response(response)
                    logger.error(
                        f"Chapa client error {response.status_code}: {error_body}"
                    )
                    return error_body

            except httpx.TimeoutException:
                last_error = f"Request timed out after {self.TIMEOUT_SECONDS}s"
                logger.warning(
                    f"Chapa timeout (attempt {attempt}/{self.MAX_RETRIES}): {url}"
                )
                await self._wait_before_retry(attempt)

            except httpx.ConnectError as e:
                last_error = f"Connection failed: {e}"
                logger.warning(
                    f"Chapa connection error (attempt {attempt}/{self.MAX_RETRIES}): {e}"
                )
                await self._wait_before_retry(attempt)

            except Exception as e:
                last_error = f"Unexpected error: {e}"
                logger.error(f"Unexpected Chapa API error: {e}", exc_info=True)
                break  # Don't retry on unexpected errors

        logger.error(
            f"Chapa API request failed after {self.MAX_RETRIES} attempts: {last_error}"
        )
        return None

    # ── Helpers ──────────────────────────────────────────────

    @staticmethod
    def _generate_tx_ref() -> str:
        """Generate a unique transaction reference."""
        short_id = uuid.uuid4().hex[:12].upper()
        return f"ZF-{short_id}"

    @staticmethod
    def _parse_error_response(response: httpx.Response) -> dict[str, Any]:
        """Extract error details from a Chapa error response."""
        try:
            body = response.json()
            return body
        except Exception:
            return {
                "status": "error",
                "message": f"HTTP {response.status_code}: {response.text[:200]}",
            }

    @staticmethod
    async def _wait_before_retry(attempt: int) -> None:
        """Exponential backoff: 1s, 2s, 4s, ..."""
        import asyncio
        delay = ChapaClient.RETRY_DELAY_SECONDS * (2 ** (attempt - 1))
        await asyncio.sleep(delay)


# ─────────────────────────────────────────────────────────────
# Module-level singleton
# ─────────────────────────────────────────────────────────────

_client: ChapaClient | None = None


def get_chapa_client() -> ChapaClient:
    """Get or create the Chapa client singleton."""
    global _client
    if _client is None:
        _client = ChapaClient()
    return _client
