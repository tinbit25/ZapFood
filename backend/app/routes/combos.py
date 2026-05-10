from fastapi import APIRouter, HTTPException
from typing import List, Dict, Any
from app.models.combo import ComboRequest, ComboRecommendation
from ai.combo_engine import ComboEngine

router = APIRouter(prefix="/combos", tags=["Combo Engine"])
builder = ComboEngine()

@router.post("/recommended/{meal_id}", response_model=List[ComboRecommendation])
async def get_recommended_combo_for_meal(meal_id: str, request: ComboRequest):
    try:
        # Find the target meal from the candidate list or cart
        # In a real app we'd fetch it from DB if not in candidate list
        target_meal = next((m for m in request.candidate_meals if m.id == meal_id), None)
        if not target_meal:
            target_meal = next((m for m in request.context.cart_meals if m.id == meal_id), None)
            
        if not target_meal:
            raise HTTPException(status_code=404, detail="Target meal not found in candidates or context.")

        return builder.build_single_upsell(target_meal, request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/cart-suggestions", response_model=List[ComboRecommendation])
async def get_cart_suggestions(request: ComboRequest):
    try:
        return builder.build_cart_suggestions(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
