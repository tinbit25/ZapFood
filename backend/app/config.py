from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    app_name: str = "Ethiopian AI Recommendations"
    debug_mode: bool = True
    firebase_credentials_path: Optional[str] = None
    chapa_secret_key: Optional[str] = None
    allowed_origins: list = ["*"]
    
    class Config:
        env_file = ".env"

settings = Settings()
