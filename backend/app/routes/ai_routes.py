import time
from datetime import datetime
from fastapi import APIRouter, HTTPException, Depends
from typing import List

from app.models.ai_api import AIPersonalizedRequest, AICartComboRequest, AIRecommendationResponse, ScoredMealResponse, AIAnalyticsEventRequest, AIChatRequest, AIChatResponse
from app.repositories.meal_repository import MealRepository
from app.repositories.user_repository import UserRepository
from ai.recommendation_engine import RecommendationEngine
from ai.similarity_engine import MLSimilarityEngine
from ai.combo_engine import ComboEngine

router = APIRouter(prefix="/api/ai", tags=["AI Recommendations (Stateful)"])

# Initialize Engines
rec_engine = RecommendationEngine()
sim_engine = MLSimilarityEngine()
combo_engine = ComboEngine()

# Initialize Repositories (In production, these would be injected dependencies)
meal_repo = MealRepository()
user_repo = UserRepository()

def format_response(scored_meals, start_time: float) -> AIRecommendationResponse:
    # Build similarity matrix periodically or on startup ideally, but doing it here for the mock
    sim_engine.precompute_similarity_matrix(meal_repo.get_available_meals())
    
    responses = []
    for sm in scored_meals:
        responses.append(ScoredMealResponse(
            mealId=sm.meal.id,
            name=sm.meal.name,
            imageUrl=sm.meal.imageUrl,
            price=sm.meal.price,
            vendorId=sm.meal.vendorId,
            matchScore=sm.final_score,
            reason=sm.cultural_reasoning or "Recommended for you",
            tags=sm.meal.tags
        ))
        
    return AIRecommendationResponse(
        recommendedMeals=responses,
        generatedAt=datetime.utcnow().isoformat() + "Z",
        processingTimeMs=(time.time() - start_time) * 1000
    )

@router.post("/recommendations", response_model=AIRecommendationResponse)
async def get_personalized_recommendations(request: AIPersonalizedRequest):
    start_time = time.time()
    try:
        pref = user_repo.get_user_preferences(request.userId)
        
        # Override fasting mode if requested explicitly
        if request.fastingMode:
            pref.fastingMode = True
            
        candidate_meals = meal_repo.get_available_meals()
        
        scored = rec_engine.score_meals(
            candidate_meals=candidate_meals,
            pref=pref,
            current_time=request.mealTime,
            current_day=request.currentDay
        )
        
        return format_response(scored, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/meals/{meal_id}/similar", response_model=AIRecommendationResponse)
async def get_similar_meals(meal_id: str):
    start_time = time.time()
    try:
        candidate_meals = meal_repo.get_available_meals()
        scored = sim_engine.compute_similar_meals(
            target_meal_id=meal_id,
            candidate_meals=candidate_meals,
            top_n=5
        )
        return format_response(scored, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/combos/{meal_id}", response_model=AIRecommendationResponse)
async def get_combos(meal_id: str):
    start_time = time.time()
    try:
        # Combo logic might need adaptation to return ScoredMeals for this API format
        # The prompt says GET /api/ai/combos/{mealId}
        # In ComboEngine we return List[ComboRecommendation]. Let's adapt it to ScoredMeal format.
        target_meals = meal_repo.get_meals_by_ids([meal_id])
        if not target_meals:
            return format_response([], start_time)
            
        target_meal = target_meals[0]
        candidate_meals = meal_repo.get_available_meals()
        
        # We need to map ComboRecommendation to ScoredMeal for the response
        combos = combo_engine.generate_combos(target_meal, candidate_meals, user_pref=user_repo.get_user_preferences("default"))
        
        from app.models.recommendation import ScoredMeal
        scored_combos = []
        for c in combos:
            scored_combos.append(ScoredMeal(
                meal=c.meal,
                final_score=c.compatibility_score,
                score_breakdown={},
                cultural_reasoning=c.upsell_reason
            ))
            
        return format_response(scored_combos, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/trending", response_model=AIRecommendationResponse)
async def get_trending_meals():
    start_time = time.time()
    try:
        pref = user_repo.get_user_preferences("default")
        candidate_meals = meal_repo.get_available_meals()
        
        original_weights = rec_engine.WEIGHTS.copy()
        rec_engine.WEIGHTS = {
            "preference": 0.05,
            "similarity": 0.0,
            "history": 0.0,
            "fasting": 0.0,
            "popularity": 0.95,
            "time": 0.0,
            "vendor": 0.0
        }
        
        scored = rec_engine.score_meals(candidate_meals, pref, "Lunch", "Monday")
        rec_engine.WEIGHTS = original_weights
        
        return format_response(scored, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/fasting", response_model=AIRecommendationResponse)
async def get_fasting_meals():
    start_time = time.time()
    try:
        pref = user_repo.get_user_preferences("fasting_user") # Mocking a fasting user
        candidate_meals = meal_repo.get_available_meals()
        fasting_candidates = [m for m in candidate_meals if m.fastingFriendly]
        
        original_weights = rec_engine.WEIGHTS.copy()
        rec_engine.WEIGHTS["fasting"] = 0.60
        
        scored = rec_engine.score_meals(fasting_candidates, pref, "Lunch", "Wednesday")
        rec_engine.WEIGHTS = original_weights
        
        return format_response(scored, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/breakfast", response_model=AIRecommendationResponse)
async def get_breakfast_meals():
    start_time = time.time()
    try:
        pref = user_repo.get_user_preferences("default")
        candidate_meals = meal_repo.get_available_meals()
        
        original_weights = rec_engine.WEIGHTS.copy()
        rec_engine.WEIGHTS["time"] = 0.80
        
        scored = rec_engine.score_meals(candidate_meals, pref, "Morning", "Monday")
        rec_engine.WEIGHTS = original_weights
        
        return format_response(scored, start_time)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/vendors/recommended/{user_id}")
async def get_recommended_vendors(user_id: str):
    # Stub for personalized vendor feed
    return {"status": "success", "message": f"Vendor recommendations for {user_id} would appear here."}

@router.post("/analytics/event")
async def log_analytics_event(event: AIAnalyticsEventRequest):
    try:
        from app.services.firebase_client import get_firestore_client, is_firebase_connected
        import time
        if is_firebase_connected():
            db = get_firestore_client()
            db.collection("ai_analytics").add({
                "userId": event.userId,
                "eventType": event.eventType,
                "mealId": event.mealId,
                "recommendationContext": event.recommendationContext,
                "timestamp": int(time.time() * 1000)
            })
        return {"status": "success"}
    except Exception as e:
        # Don't fail the client request if analytics fails
        print(f"Failed to log analytics: {e}")
        return {"status": "error", "detail": str(e)}

@router.post("/chat", response_model=AIChatResponse)
async def chat_with_ai(request: AIChatRequest):
    try:
        from app.ai.gemini_client import GeminiClient
        import json
        gemini = GeminiClient()
        
        pref = user_repo.get_user_preferences(request.userId)
        candidate_meals = meal_repo.get_available_meals()
        
        # Build prompt
        menu_context = "\n".join([f"- {m.id}: {m.name} ({m.price} ETB) - Tags: {m.tags}" for m in candidate_meals[:20]]) # limit to 20 for prompt size
        
        prompt = f"""
You are ZapFood's AI Dietitian, an expert on Ethiopian cuisine and healthy eating.
A user asked: "{request.message}"

Their current dietary preference:
Fasting Mode: {pref.fastingMode}
Spice Preference: {pref.spicePreference}
Favorite Categories: {pref.favoriteCategories}

Available menu items:
{menu_context}

Respond in JSON format EXACTLY like this:
{{
  "replyText": "A friendly, helpful conversational response to the user's message.",
  "recommendedMealIds": ["id1", "id2"]
}}

Only recommend meal IDs from the available menu that fit their request. Max 3 recommendations.
"""
        
        # Use existing JSON parsing in gemini client with mock fallback if Gemini fails/key missing
        try:
            response_dict = gemini.generate_recommendations(prompt)
            reply_text = response_dict.get("replyText", "I found some great options for you!")
            recommended_ids = response_dict.get("recommendedMealIds", [])
        except Exception as gemini_err:
            print(f"Gemini API call failed, using mock fallback: {gemini_err}")
            # Heuristic-based mock dietitian fallback
            msg_lower = request.message.lower()
            if pref.fastingMode or any(keyword in msg_lower for keyword in ["fasting", "vegan", "vegetary", "plant"]):
                reply_text = "As you are currently fasting or looking for plant-based choices, I've selected some delicious fasting-friendly options like Shiro Wot and Beyaynetu from our local vendors!"
                recommended_ids = [m.id for m in candidate_meals if m.fastingFriendly][:3]
            elif any(keyword in msg_lower for keyword in ["spicy", "hot", "pepp"]):
                reply_text = "If you're craving something spicy and bold, you should try our delicious Tibs or Doro Wat, seasoned with traditional berbere!"
                recommended_ids = [m.id for m in candidate_meals if "spicy" in [t.lower() for t in m.tags] or m.spiceLevel == "HIGH"][:3]
            elif any(keyword in msg_lower for keyword in ["morning", "breakfast", "breakfasts"]):
                reply_text = "For a nutritious breakfast, here are some great morning meal options like Chechebsa or Firfir!"
                recommended_ids = [m.id for m in candidate_meals if "breakfast" in [t.lower() for t in m.tags] or "breakfast" in m.category.lower()][:3]
            else:
                reply_text = "Hello! I am your ZapFood AI Dietitian. Based on your preferences, here are some wonderful local dishes that you might enjoy today!"
                recommended_ids = [m.id for m in candidate_meals][:3]
        
        # Map IDs back to ScoredMealResponse
        suggested = []
        for m in candidate_meals:
            if m.id in recommended_ids:
                suggested.append(ScoredMealResponse(
                    mealId=m.id,
                    name=m.name,
                    imageUrl=m.imageUrl,
                    price=m.price,
                    vendorId=m.vendorId,
                    matchScore=95.0,
                    reason="Matched your request perfectly.",
                    tags=m.tags
                ))
                
        return AIChatResponse(
            replyText=reply_text,
            suggestedMeals=suggested,
            generatedAt=datetime.utcnow().isoformat() + "Z"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
