import logging
from functools import lru_cache
from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # ── App Info ────────────────────────────────────────────────
    app_name: str = "Ethiopian AI Recommendations"
    backend_debug: bool = True
    backend_base_url: str = "http://localhost:8000"
    
    # ── Firebase ───────────────────────────────────────────────
    firebase_credentials_path: Optional[str] = "./firebase-service-account.json"
    
    # ── Chapa Payment ──────────────────────────────────────────
    chapa_secret_key: Optional[str] = None
    chapa_base_url: str = "https://api.chapa.co/v1"
    
    # ── Gemini AI ──────────────────────────────────────────────
    gemini_api_key: Optional[str] = None
    gemini_model_name: str = "gemini-2.0-flash"
    
    # ── Behavior Tracking ──────────────────────────────────────
    behavior_collection: str = "user_behavior"
    
    # ── Security & Auth ────────────────────────────────────────
    allowed_origins: list = ["*"]
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
            and "xxxxxxxx" not in (self.chapa_secret_key or "")
        )

    class Config:
        env_file = ".env"
        case_sensitive = False

@lru_cache()
def get_settings() -> Settings:
    """
    Cached settings singleton.
    Call this instead of constructing Settings() directly.
    """
    return Settings()
