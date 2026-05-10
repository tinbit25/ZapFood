import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Dict, Tuple
from app.models.recommendation import Meal, ScoredMeal

class MLSimilarityEngine:
    def __init__(self):
        # We instantiate TF-IDF vectorizers on demand to avoid state issues across requests,
        # but in production, these would be fit periodically and cached.
        pass

    def compute_similar_meals(self, target_meal_id: str, candidate_meals: List[Meal], top_n: int = 5) -> List[ScoredMeal]:
        if not candidate_meals:
            return []

        # Find target meal
        target_meal = next((m for m in candidate_meals if m.id == target_meal_id), None)
        if not target_meal:
            return []

        # Remove target meal from candidates
        pool = [m for m in candidate_meals if m.id != target_meal_id]
        if not pool:
            return []

        # Build DataFrames for vectorization
        all_meals = [target_meal] + pool
        
        # 1. Ingredient Similarity (TF-IDF)
        ingredients_texts = [" ".join([i.replace(" ", "") for i in m.ingredients]) for m in all_meals]
        vectorizer_ing = TfidfVectorizer(token_pattern=r"(?u)\b\w+\b")
        try:
            tfidf_ing = vectorizer_ing.fit_transform(ingredients_texts)
            sim_ing = cosine_similarity(tfidf_ing[0:1], tfidf_ing[1:]).flatten()
        except ValueError:
            # Handle empty vocab
            sim_ing = np.zeros(len(pool))

        # 2. Tag Similarity (TF-IDF)
        tags_texts = [" ".join([t.replace(" ", "") for t in m.tags]) for m in all_meals]
        vectorizer_tags = TfidfVectorizer(token_pattern=r"(?u)\b\w+\b")
        try:
            tfidf_tags = vectorizer_tags.fit_transform(tags_texts)
            sim_tags = cosine_similarity(tfidf_tags[0:1], tfidf_tags[1:]).flatten()
        except ValueError:
            sim_tags = np.zeros(len(pool))

        # 3. Categorical/Boolean Matches
        sim_cat = np.zeros(len(pool))
        for i, m in enumerate(pool):
            score = 0.0
            if m.category == target_meal.category:
                score += 0.5
            if m.fastingFriendly == target_meal.fastingFriendly:
                score += 0.3
            if m.spiceLevel == target_meal.spiceLevel:
                score += 0.2
            sim_cat[i] = score

        # 4. Expansion / Bundling Logic (Cultural Specific)
        sim_expansion = np.zeros(len(pool))
        target_name_lower = target_meal.name.lower()
        for i, m in enumerate(pool):
            m_name_lower = m.name.lower()
            bonus = 0.0
            
            # Kitfo expansions
            if "kitfo" in target_name_lower:
                if "kocho" in m_name_lower or "ayib" in m_name_lower or "gomen" in m_name_lower:
                    bonus += 1.0
            
            # Tibs expansions
            if "tibs" in target_name_lower:
                if "awaze" in m_name_lower or "shekla" in m_name_lower:
                    bonus += 1.0
                    
            # Shiro expansions
            if "shiro" in target_name_lower:
                if "misir" in m_name_lower or "kik" in m_name_lower or "salata" in m_name_lower:
                    bonus += 1.0

            sim_expansion[i] = min(1.0, bonus)

        # 5. Composite Score
        # Weights: Ingredients (0.4), Tags (0.3), Category (0.2), Expansion (0.1)
        final_scores = (sim_ing * 0.4) + (sim_tags * 0.3) + (sim_cat * 0.2) + (sim_expansion * 0.1)

        scored_meals = []
        for i, m in enumerate(pool):
            final_score = float(final_scores[i])
            if final_score <= 0.0:
                continue

            breakdown = {
                "ingredients_tfidf": float(sim_ing[i]) * 0.4,
                "tags_tfidf": float(sim_tags[i]) * 0.3,
                "category_match": float(sim_cat[i]) * 0.2,
                "expansion_bonus": float(sim_expansion[i]) * 0.1
            }

            reasoning = self._generate_explainability(target_meal, m, breakdown)

            scored_meals.append(ScoredMeal(
                meal=m,
                final_score=final_score,
                score_breakdown=breakdown,
                cultural_reasoning=reasoning
            ))

        scored_meals.sort(key=lambda x: x.final_score, reverse=True)
        return scored_meals[:top_n]

    def _generate_explainability(self, target: Meal, candidate: Meal, breakdown: Dict[str, float]) -> str:
        highest = max(breakdown, key=breakdown.get)
        if highest == "expansion_bonus":
            return f"Highly recommended to pair with {target.name}."
        elif highest == "ingredients_tfidf":
            return f"Recommended because it shares a similar ingredient profile with {target.name}."
        elif highest == "tags_tfidf":
            return f"Shares similar culinary features and tags with {target.name}."
        elif highest == "category_match":
            return f"Another great option in the {target.category} category."
        else:
            return f"Similar to {target.name}."
