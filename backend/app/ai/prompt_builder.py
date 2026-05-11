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
        """
        
        meals_context = "\n".join([
            f"- ID: {m.get('id')}, Name: {m.get('name')}, Vendor: {m.get('vendorName')}, "
            f"Tags: {m.get('tags')}, MealTime: {m.get('mealTime')}, "
            f"SpiceLevel: {m.get('spiceLevel')}, Price: {m.get('price')}"
            for m in available_meals
        ])

        system_instruction = """
        You are the 'ZapFood Smart Assistant'. Your mission is to provide the most authentic, 
        culturally-aware food recommendations for users in Ethiopia.
        
        Avoid using the word 'AI' in your reasoning. Use terms like 'Smart Picks' or 'Recommended for you'.

        ### ETHIOPIAN CULTURAL KNOWLEDGE:
        1. BREAKFAST: Ful, Chechebsa, Kinche, Firfir, Genfo.
        2. LUNCH: Shiro, Tibs, Beyaynetu, stews with Injera.
        3. DINNER: Often stews or Tibs, sometimes lighter.
        4. FASTING: Wednesdays and Fridays are strictly vegan (no meat/dairy). 
        5. SPICE: Berbere/Mitmita are key. Respect spice preferences (LOW, MEDIUM, HIGH).

        ### OPERATING RULES:
        1. ONLY recommend meal IDs from the "AVAILABLE MEALS" list. 
        2. FASTING ADHERENCE: If 'fastingMode' is true, NEVER recommend meat or dairy.
        3. MEAL TIME: If it's BREAKFAST time, prioritize breakfast items.
        4. REASONING: Explain your choice using Ethiopian cultural context and user preferences.
        """

        prompt = f"""
        {system_instruction}

        ### USER PREFERENCES & HABITS:
        - Favorite Foods: {", ".join(user_habits.get('favoriteFoods', []))}
        - Top Categories: {", ".join(user_habits.get('topCategories', []))}
        - Spice Preference: {user_habits.get('spicePreference', 'MEDIUM')}
        - Budget: {user_habits.get('budgetPreference', 'STANDARD')}
        - Recent History (IDs): {user_habits.get('orderHistory', [])[:5]}

        ### CURRENT CONTEXT:
        - Current Time Slot: {context.get('mealTime', 'unknown')}
        - Fasting Day: {context.get('fastingMode', False)} ({context.get('dayOfWeek', 'unknown')})
        - Is it Wed/Fri: {context.get('isWednesdayOrFriday', False)}

        ### AVAILABLE MEALS (GROUND TRUTH):
        {meals_context}

        ### RESPONSE SCHEMA (JSON ONLY):
        {{
          "recommendedMealIds": ["id1", "id2"],
          "reasoning": "Culturally-aware explanation",
          "nutritionSummary": "Brief overview"
        }}
        """

        return prompt
