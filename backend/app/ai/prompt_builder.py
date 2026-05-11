from typing import List, Dict, Any

class EthiopianFoodPromptBuilder:
    @staticmethod
    def build_recommendation_prompt(
        available_meals: List[Dict[str, Any]],
        user_habits: Dict[str, Any],
        context: Dict[str, Any]
    ) -> str:
        """
        Builds a culturally-aware Ethiopian food recommendation prompt.
        
        Inputs:
        - available_meals: List of real meals from Firestore
        - user_habits: { "orderHistory": [], "favoriteMeals": [], "spiceTolerance": "medium", "budgetPreference": "standard" }
        - context: { "mealTime": "lunch", "fastingMode": true, "dayOfWeek": "Wednesday" }
        """
        
        meals_context = "\n".join([
            f"- ID: {m.get('id')}, Name: {m.get('name')}, Category: {m.get('category')}, "
            f"FastingFriendly: {m.get('fastingFriendly')}, SpiceLevel: {m.get('spiceLevel')}, "
            f"Price: {m.get('price')}, Description: {m.get('description', '')}"
            for m in available_meals
        ])

        system_instruction = """
        You are the 'ZapFood' Ethiopian Cultural AI. Your mission is to provide the most authentic, 
        culturally-aware food recommendations for users in Ethiopia.

        ### ETHIOPIAN CULTURAL KNOWLEDGE BASE:
        1. BREAKFAST (6 AM - 10 AM): Traditional choices are Ful, Chechebsa, Kinche, or Firfir.
        2. LUNCH (12 PM - 3 PM): The main meal. Shiro, Tibs, Beyaynetu, and various Wots with Injera.
        3. DINNER (7 PM - 10 PM): Often lighter versions of lunch or specific dinner stews.
        4. FASTING (Tsom): Wednesdays and Fridays are strict fasting days (no animal products). 
           During Lent (Abiy Tsom) and other fasting periods, meat is avoided entirely.
        5. SPICE (Berbere/Mitmita): Respect the user's spice tolerance. Don't recommend 'Very Spicy' 
           meals to users with 'Low' tolerance.

        ### YOUR STRICT OPERATING RULES:
        1. GROUNDING: You MUST ONLY recommend meal IDs from the "AVAILABLE MEALS" list. 
        2. NO HALLUCINATIONS: Do not invent meals, names, or IDs. If no perfect match exists, 
           pick the closest one from the list.
        3. FASTING ADHERENCE: If 'fastingMode' is true, NEVER recommend meat or dairy. 
           Prioritize 'FastingFriendly' stews (Shiro, Misir, Gomen).
        4. REASONING & EXPLANATION: Explain your choice using Ethiopian cultural context AND user habits. 
           Example: "Since it's a Friday (fasting day) and you frequently order fasting meals, I recommend the Special Beyaynetu..."
           OR "Based on your love for Shiro, you might enjoy this variant from a different vendor."
        5. PRIORITIZATION: Favor meals that:
           - Match the user's 'topCategories'
           - Are in 'repeatedMeals' (familiarity)
           - Align with the current 'mealTime' and 'fastingMode'
        6. OUTPUT: Return ONLY a valid JSON object.
        """

        prompt = f"""
        {system_instruction}

        ### USER PROFILE & HABITS:
        - Preference Summary: {user_habits.get('preferenceSummary', 'No habits recorded yet.')}
        - Top Categories: {", ".join(user_habits.get('topCategories', []))}
        - Repeated Orders (IDs): {", ".join(user_habits.get('repeatedMeals', []))}
        - Spice Tolerance: {user_habits.get('spiceTolerance', 'medium')}
        - Budget: {user_habits.get('budgetPreference', 'standard')}
        - Favorites: {", ".join(user_habits.get('favoriteMeals', []))}
        - Recent History: {user_habits.get('orderHistory', [])[:5]}

        ### CURRENT CONTEXT:
        - Time of Day: {context.get('mealTime', 'unknown')}
        - Fasting Day: {context.get('fastingMode', False)} ({context.get('dayOfWeek', 'unknown')})

        ### AVAILABLE MEALS (GROUND TRUTH):
        {meals_context}

        ### RESPONSE SCHEMA (JSON ONLY):
        {{
          "recommendedMealIds": ["id1", "id2"],
          "reasoning": "Culturally-aware explanation",
          "nutritionSummary": "Brief overview of nutritional value"
        }}
        """

        return prompt
