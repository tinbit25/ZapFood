"""
Configuration — Pydantic Settings loaded from .env

All secrets and environment-specific values are centralized here.
No other module should read environment variables directly.
"""

from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application settings loaded from .env file."""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    # ── Chapa API ────────────────────────────────────────────
    chapa_secret_key: str = "CHASECK-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    chapa_public_key: str = "CHAPUBK-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    chapa_webhook_secret: str = ""
    chapa_base_url: str = "https://api.chapa.co/v1"

    # ── Firebase ─────────────────────────────────────────────
    firebase_credentials_path: str = "./firebase-service-account.json"

    # ── Backend Server ───────────────────────────────────────
    backend_base_url: str = "http://localhost:8000"
    backend_debug: bool = True

    # ── JWT Auth ─────────────────────────────────────────────
    jwt_secret_key: str = "dev-secret-key-change-in-production"
    jwt_algorithm: str = "HS256"
    jwt_expiration_minutes: int = 60

    # ── Currency ─────────────────────────────────────────────
    default_currency: str = "ETB"

    @property
    def is_chapa_configured(self) -> bool:
        """Check if Chapa secret key has been set to a real value."""
        return (
            bool(self.chapa_secret_key)
            and "xxxxxxxx" not in self.chapa_secret_key
        )


@lru_cache()
def get_settings() -> Settings:
    """
    Cached settings singleton.
    Call this instead of constructing Settings() directly.
    """
    return Settings()
