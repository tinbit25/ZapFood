"""
Health Route — System health check endpoint.

Provides a quick status overview of all backend subsystems:
Firebase connectivity, Chapa configuration, currency, and debug mode.
"""

from fastapi import APIRouter

from app.config import get_settings
from app.models.schemas import ApiResponse, HealthStatus
from app.services.firebase_client import is_firebase_connected

router = APIRouter(tags=["System"])


@router.get(
    "/health",
    response_model=ApiResponse[HealthStatus],
    summary="Health Check",
    description="Returns the current health status of the payment backend.",
)
async def health_check() -> ApiResponse[HealthStatus]:
    """
    Quick health check covering:
    - Firebase Firestore connectivity
    - Chapa API key configuration
    - Active currency
    - Debug mode flag
    """
    settings = get_settings()

    status = HealthStatus(
        status="healthy",
        version="1.0.0",
        firebase_connected=is_firebase_connected(),
        chapa_configured=settings.is_chapa_configured,
        currency=settings.default_currency,
        debug_mode=settings.backend_debug,
    )

    return ApiResponse.ok(data=status, message="Payment backend is running")
