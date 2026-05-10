from app.models.combo import CartContext
from app.models.recommendation import Meal

class ComboScorer:
    def score_addon(self, candidate: Meal, context: CartContext, base_rule_score: float) -> float:
        score = base_rule_score
        
        # 1. Vendor Match (Avoid split deliveries)
        cart_vendors = {m.vendorId for m in context.cart_meals}
        if candidate.vendorId in cart_vendors:
            score += 0.3
            
        # 2. Popularity Boost
        # Simple normalization based on an assumed max popularity of 100
        pop_boost = min(0.2, (candidate.popularityScore / 100.0) * 0.2)
        score += pop_boost
        
        # 3. Price Complementarity (Usually add-ons shouldn't be drastically more expensive than the main meal)
        if context.cart_meals:
            avg_cart_price = sum(m.price for m in context.cart_meals) / len(context.cart_meals)
            if candidate.price <= avg_cart_price * 0.5:
                score += 0.1 # Bonus for cheap add-ons
                
        return min(1.0, score)
