import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from typing import List, Dict, Tuple, Optional
from app.models.recommendation import Meal, ScoredMeal
from ai.ethiopian_food_knowledge import MEAL_RELATIONSHIPS

class MLSimilarityEngine:
    def __init__(self):
        # We instantiate TF-IDF vectorizers on demand to avoid state issues across requests,
        # but in production, these would be fit periodically and cached.
        self.similarity_matrix = None
        self.meal_index_map = {}

    def precompute_similarity_matrix(self, all_meals: List[Meal]):
        """Builds a precomputed similarity matrix for fast lookups.
        This is a Netflix-style approach to similarity."""
        if not all_meals:
            return
            
        self.meal_index_map = {m.id: i for i, m in enumerate(all_meals)}
        
        # 1. Ingredient Similarity
        ingredients_texts = [" ".join([i.replace(" ", "") for i in m.ingredients]) for m in all_meals]
        vectorizer_ing = TfidfVectorizer(token_pattern=r"(?u)\b\w+\b")
        try:
            tfidf_ing = vectorizer_ing.fit_transform(ingredients_texts)
            sim_ing = cosine_similarity(tfidf_ing)
        except ValueError:
            sim_ing = np.zeros((len(all_meals), len(all_meals)))

        # 2. Tag Similarity
        tags_texts = [" ".join([t.replace(" ", "") for t in m.tags]) for m in all_meals]
        vectorizer_tags = TfidfVectorizer(token_pattern=r"(?u)\b\w+\b")
        try:
            tfidf_tags = vectorizer_tags.fit_transform(tags_texts)
            sim_tags = cosine_similarity(tfidf_tags)
        except ValueError:
            sim_tags = np.zeros((len(all_meals), len(all_meals)))
            
        # Composite score matrix
        self.similarity_matrix = (sim_ing * 0.6) + (sim_tags * 0.4)

    def get_precomputed_similar(self, target_meal_id: str, all_meals: List[Meal], top_n: int = 5) -> List[Meal]:
        if self.similarity_matrix is None or target_meal_id not in self.meal_index_map:
            return []
            
        idx = self.meal_index_map[target_meal_id]
        sim_scores = list(enumerate(self.similarity_matrix[idx]))
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        
        # Skip self
        similar_indices = [i[0] for i in sim_scores[1:top_n+1]]
        return [all_meals[i] for i in similar_indices]

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
            
            # Use Knowledge Base Relationships
            for base_meal, related_meals in MEAL_RELATIONSHIPS.items():
                if base_meal in target_name_lower and any(r in m_name_lower for r in related_meals):
                    bonus += 1.0
                    break
                    
            # Doro wat specific
            if "doro wat" in target_name_lower:
                if "chicken tibs" in m_name_lower or "special doro" in m_name_lower:
                    bonus += 1.0

            sim_expansion[i] = min(1.0, bonus)

        # 5. Composite Score
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
            return f"Because you liked {target.name}, you might love this."
        elif highest == "ingredients_tfidf":
            return f"Recommended because it shares ingredients and spice profile with {target.name}."
        elif highest == "tags_tfidf":
            return f"Shares similar culinary features and tags with {target.name}."
        elif highest == "category_match":
            return f"Another great option in the {target.category} category."
        else:
            return f"Similar to {target.name}."
