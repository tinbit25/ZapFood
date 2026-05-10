from typing import List, Dict, Any, Tuple
from app.models.recommendation import Meal
from app.models.combo import CartContext
from app.services.combo.knowledge_base import ETHIOPIAN_COMBO_RULES, CONTEXTUAL_RULES

class ComboRulesEngine:
    def get_valid_addons(self, context: CartContext, candidate_meals: List[Meal]) -> List[Tuple[Meal, str, float]]:
        """
        Returns a list of (Meal, reason, base_score) that pass basic rule checks.
        """
        valid_addons = []
        is_fasting_day = context.day_of_week in ["Wednesday", "Friday"]
        
        # 1. Determine base target names from cart
        cart_names = [m.name.lower() for m in context.cart_meals]
        
        for candidate in candidate_meals:
            # Skip items already in cart
            if any(candidate.id == cm.id for cm in context.cart_meals):
                continue
                
            # Filter fasting logically (Strict filter for Wed/Fri if it's a fasting user, 
            # or just generally don't suggest meat combos on fasting days if the cart is fasting)
            cart_is_fasting = all(cm.fastingFriendly for cm in context.cart_meals) if context.cart_meals else False
            if is_fasting_day and cart_is_fasting and not candidate.fastingFriendly:
                continue

            c_name = candidate.name.lower()
            best_reason = ""
            base_score = 0.0

            # 2. Check direct knowledge base
            for target_key, rules in ETHIOPIAN_COMBO_RULES.items():
                if any(target_key in cn for cn in cart_names):
                    if any(addon in c_name for addon in rules["addons"]):
                        best_reason = rules["reason"]
                        base_score = 0.8
                        break

            # 3. Check contextual rules (Weather)
            weather_rule = CONTEXTUAL_RULES.get(context.weather)
            if weather_rule:
                if any(bt in c_name for bt in weather_rule["boost_tags"]) or any(bt in tag.lower() for tag in candidate.tags for bt in weather_rule["boost_tags"]):
                    if base_score == 0.0:
                        best_reason = weather_rule["reason"]
                    base_score += 0.3
                    
            if base_score > 0.0:
                valid_addons.append((candidate, best_reason, min(1.0, base_score)))
                
        return valid_addons
