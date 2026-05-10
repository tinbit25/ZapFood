"""
Chapa Payment Backend — FastAPI Application Entry Point

Architecture: Android App → FastAPI Backend → Chapa API
Currency: ETB (Ethiopian Birr) only.

This module:
- Configures CORS for Android client access
- Initializes Firebase Admin SDK on startup
- Mounts all route modules
- Provides structured logging
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.routes.health import router as health_router
from app.routes.payments import router as payments_router
from app.routes.recommendations import router as recommendations_router
from app.routes.similarity import router as similarity_router
from app.routes.combos import router as combos_router
from app.services.firebase_client import initialize_firebase

# ─────────────────────────────────────────────────────────────
# Logging
# ─────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s │ %(levelname)-8s │ %(name)s │ %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger("chapa-backend")


# ─────────────────────────────────────────────────────────────
# Lifespan — Startup / Shutdown events
# ─────────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifespan handler.
    - Startup: Initialize Firebase, log configuration status.
    - Shutdown: Cleanup resources.
    """
    settings = get_settings()

    logger.info("=" * 60)
    logger.info("  Chapa Payment Backend — Starting Up")
    logger.info("=" * 60)
    logger.info(f"  Debug Mode  : {settings.backend_debug}")
    logger.info(f"  Currency    : {settings.default_currency}")
    logger.info(f"  Chapa URL   : {settings.chapa_base_url}")
    logger.info(f"  Backend URL : {settings.backend_base_url}")

    # Initialize Firebase
    firebase_ok = initialize_firebase()
    if firebase_ok:
        logger.info("  Firebase    : ✓ Connected")
    else:
        logger.warning("  Firebase    : ✗ Not connected (check credentials)")

    # Check Chapa configuration
    if settings.is_chapa_configured:
        logger.info("  Chapa Key   : ✓ Configured")
    else:
        logger.warning("  Chapa Key   : ✗ Not configured (using placeholder)")

    logger.info("=" * 60)
    logger.info("  Backend ready — listening for requests")
    logger.info("=" * 60)

    yield  # App is running

    # Shutdown
    logger.info("Chapa Payment Backend — Shutting down")


# ─────────────────────────────────────────────────────────────
# FastAPI App
# ─────────────────────────────────────────────────────────────
app = FastAPI(
    title="Chapa Payment Backend",
    description=(
        "Secure backend-mediated payment processing for the Ethiopian "
        "food delivery app. Handles Chapa API integration, webhook "
        "processing, and Firestore synchronization."
    ),
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs" if get_settings().backend_debug else None,
    redoc_url="/redoc" if get_settings().backend_debug else None,
)

# ─────────────────────────────────────────────────────────────
# CORS — Allow Android app and local development
# ─────────────────────────────────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:3000",       # Local web dev
        "http://localhost:8080",       # Android emulator
        "http://10.0.2.2:8000",        # Android emulator → host
        "*",                           # TODO: Restrict in production
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─────────────────────────────────────────────────────────────
# Route Registration
# ─────────────────────────────────────────────────────────────
app.include_router(health_router, prefix="/api")
app.include_router(payments_router, prefix="/api")
app.include_router(recommendations_router, prefix="/api")
app.include_router(similarity_router, prefix="/api")
app.include_router(combos_router, prefix="/api")


# ─────────────────────────────────────────────────────────────
# Root redirect (convenience)
# ─────────────────────────────────────────────────────────────
@app.get("/", include_in_schema=False)
async def root():
    """Redirect root to API docs."""
    return {
        "service": "Chapa Payment Backend",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/api/health",
    }
