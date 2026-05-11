import logging
import json
import google.generativeai as genai
from tenacity import retry, stop_after_attempt, wait_exponential
from app.config import get_settings

logger = logging.getLogger("gemini-client")

class GeminiClient:
    def __init__(self):
        self.settings = get_settings()
        if not self.settings.gemini_api_key:
            logger.error("GEMINI_API_KEY is not configured!")
            return

        genai.configure(api_key=self.settings.gemini_api_key)
        self.model = genai.GenerativeModel(
            model_name=self.settings.gemini_model_name,
            generation_config={
                "response_mime_type": "application/json"
            }
        )

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
    def generate_recommendations(self, prompt: str) -> dict:
        """
        Sends a prompt to Gemini and parses the structured JSON response.
        """
        if not self.settings.gemini_api_key:
            raise ValueError("Gemini API Key missing")

        try:
            logger.info(f"Sending prompt to Gemini (Model: {self.settings.gemini_model_name})")
            response = self.model.generate_content(prompt)
            
            # Log token usage if available
            # logger.info(f"Gemini tokens used: {response.usage_metadata}")

            raw_text = response.text
            logger.info(f"Raw Gemini response: {raw_text}")
            
            return json.loads(raw_text)
        except Exception as e:
            logger.error(f"Gemini API Error: {e}")
            raise
