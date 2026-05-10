from typing import List, Tuple
from app.models.combo import CartContext, ComboRequest, ComboRecommendation
from app.models.recommendation import Meal
from ai.ethiopian_food_knowledge import ETHIOPIAN_COMBO_RULES, CONTEXTUAL_RULES

class ComboRulesEngine:
    def get_valid_addons(self, context: CartContext, candidate_meals: List[Meal]) -> List[Tuple[Meal, str, float]]:
        valid_addons = []
        is_fasting_day = context.day_of_week in ["Wednesday", "Friday"]
        
        cart_names = [m.name.lower() for m in context.cart_meals]
        
        for candidate in candidate_meals:
            if any(candidate.id == cm.id for cm in context.cart_meals):
                continue
                
            cart_is_fasting = all(cm.fastingFriendly for cm in context.cart_meals) if context.cart_meals else False
            if is_fasting_day and cart_is_fasting and not candidate.fastingFriendly:
                continue

            c_name = candidate.name.lower()
            best_reason = ""
            base_score = 0.0

            for target_key, rules in ETHIOPIAN_COMBO_RULES.items():
                if any(target_key in cn for cn in cart_names):
                    if any(addon in c_name for addon in rules["addons"]):
                        best_reason = rules["reason"]
                        base_score = 0.8
                        break

            weather_rule = CONTEXTUAL_RULES.get(context.weather)
            if weather_rule:
                if any(bt in c_name for bt in weather_rule["boost_tags"]) or any(bt in tag.lower() for tag in candidate.tags for bt in weather_rule["boost_tags"]):
                    if base_score == 0.0:
                        best_reason = weather_rule["reason"]
                    base_score += 0.3
                    
            if base_score > 0.0:
                valid_addons.append((candidate, best_reason, min(1.0, base_score)))
                
        return valid_addons

class ComboScorer:
    def score_addon(self, candidate: Meal, context: CartContext, base_rule_score: float) -> float:
        score = base_rule_score
        
        cart_vendors = {m.vendorId for m in context.cart_meals}
        if candidate.vendorId in cart_vendors:
            score += 0.3
            
        pop_boost = min(0.2, (candidate.popularityScore / 100.0) * 0.2)
        score += pop_boost
        
        if context.cart_meals:
            avg_cart_price = sum(m.price for m in context.cart_meals) / len(context.cart_meals)
            if candidate.price <= avg_cart_price * 0.5:
                score += 0.1 
                
        return min(1.0, score)

class ComboEngine:
    def __init__(self):
        self.rules_engine = ComboRulesEngine()
        self.scorer = ComboScorer()

    def build_cart_suggestions(self, request: ComboRequest) -> List[ComboRecommendation]:
        if not request.context.cart_meals:
            return []

        valid_addons = self.rules_engine.get_valid_addons(request.context, request.candidate_meals)
        recommendations = []
        for candidate, reason, base_score in valid_addons:
            final_score = self.scorer.score_addon(candidate, request.context, base_score)
            
            if not reason:
                if len(request.context.cart_meals) == 1:
                    reason = f"Frequently ordered with {request.context.cart_meals[0].name}."
                else:
                    reason = "Frequently ordered together with items in your cart."

            recommendations.append(
                ComboRecommendation(
                    meal=candidate,
                    match_score=final_score,
                    upsell_reason=reason
                )
            )

        recommendations.sort(key=lambda x: x.match_score, reverse=True)
        return recommendations[:request.top_n]

    def build_single_upsell(self, target_meal: Meal, request: ComboRequest) -> List[ComboRecommendation]:
        temp_context = CartContext(
            cart_meals=[target_meal],
            time_of_day=request.context.time_of_day,
            day_of_week=request.context.day_of_week,
            weather=request.context.weather,
            user_id=request.context.user_id
        )
        
        temp_request = ComboRequest(
            context=temp_context,
            candidate_meals=request.candidate_meals,
            top_n=request.top_n
        )
        return self.build_cart_suggestions(temp_request)
