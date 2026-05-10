# Structured Cultural Intelligence Layer for Ethiopian Meals

# 1. Ethiopian Meal Categories
CATEGORIES = {
    "fasting": ["shiro", "misir", "kik alicha", "gomen", "fasolia", "salata", "fasting firfir", "timatim sils", "suf fitfit"],
    "meat": ["tibs", "kitfo", "doro wat", "minchet abish", "gored gored", "key wat", "bozena shiro", "kikil", "dulet"],
    "breakfast": ["ful", "chechebsa", "kinche", "bula", "fata", "scrambled eggs", "firfir"],
    "healthy": ["salata", "gomen", "avocado", "suf fitfit", "telba"],
    "budget": ["shiro", "kik alicha", "salata", "fata", "bread"],
    "cultural": ["doro wat", "kitfo", "kocho", "raw meat (tere siga)"],
    "platters": ["beyaynetu", "ageligil", "gebeta", "mahiberawi"],
    "drinks": ["coffee", "tea", "spiced tea", "ambo water", "mirinda", "coca cola", "tej", "tella"],
    "snacks": ["kolo", "dabo kolo", "popcorn", "biscuit"]
}

# 2. Ethiopian Cultural Meal Rules
CULTURAL_RULES = {
    "fasting_days": {
        "days": ["Wednesday", "Friday"],
        "prioritize": CATEGORIES["fasting"],
        "demote": CATEGORIES["meat"]
    },
    "morning": {
        "times": ["Morning", "Breakfast"],
        "prioritize": CATEGORIES["breakfast"]
    },
    "holidays": {
        "prioritize": ["doro wat", "kitfo", "special platters", "tej", "sheep meat (beg tibs)"]
    }
}

# 3. Meal Relationship Mapping
MEAL_RELATIONSHIPS = {
    "tibs": ["awaze tibs", "derek tibs", "shekla tibs", "zilzil tibs", "choma tibs"],
    "shiro": ["misir", "kik alicha", "gomen", "bozena shiro", "shiro tegamino"],
    "kitfo": ["gored gored", "dulet", "tere siga"],
    "doro wat": ["key wat", "minchet abish"],
    "firfir": ["dirkosh firfir", "injera firfir", "quanta firfir"]
}

# 4. Ingredient Intelligence
INGREDIENT_INTELLIGENCE = {
    "tibs": {"spice_level": "medium", "oil_level": "medium", "protein_level": "high", "fasting": False, "vegan": False},
    "shiro": {"spice_level": "low", "oil_level": "medium", "protein_level": "medium", "fasting": True, "vegan": True},
    "kitfo": {"spice_level": "high", "oil_level": "high", "protein_level": "high", "fasting": False, "vegan": False},
    "doro wat": {"spice_level": "high", "oil_level": "medium", "protein_level": "high", "fasting": False, "vegan": False},
    "misir": {"spice_level": "medium", "oil_level": "medium", "protein_level": "high", "fasting": True, "vegan": True},
    "gomen": {"spice_level": "low", "oil_level": "low", "protein_level": "low", "fasting": True, "vegan": True},
    "ful": {"spice_level": "low", "oil_level": "high", "protein_level": "high", "fasting": True, "vegan": True},
    "chechebsa": {"spice_level": "medium", "oil_level": "high", "protein_level": "low", "fasting": False, "vegan": False} # Contains butter (kibe) usually
}

# 5. Combo Intelligence (Used by Combo Engine)
ETHIOPIAN_COMBO_RULES = {
    "kitfo": {
        "addons": ["ayib", "kocho", "coca cola", "ambo water"],
        "reason": "Classic Kitfo companions."
    },
    "shiro": {
        "addons": ["injera", "avocado salad", "gomen", "mirinda"],
        "reason": "Perfect sides for Shiro."
    },
    "doro wat": {
        "addons": ["ayib", "extra egg", "injera"],
        "reason": "Enhances the Doro Wat experience."
    },
    "tibs": {
        "addons": ["awaze", "extra injera", "ambo water", "beer"],
        "reason": "Great with Tibs."
    },
    "ful": {
        "addons": ["bread", "tea", "egg", "yogurt"],
        "reason": "Standard Ful breakfast add-ons."
    },
    "chechebsa": {
        "addons": ["coffee", "tea", "honey", "yogurt"],
        "reason": "Complements Chechebsa perfectly."
    }
}

CONTEXTUAL_RULES = {
    "Rainy": {
        "boost_tags": ["soup", "hot", "tea", "coffee", "bozena shiro", "kikil", "spicy"],
        "reason": "Perfect for the rainy weather."
    },
    "Hot": {
        "boost_tags": ["cold drink", "salad", "water", "juice", "ice cream"],
        "reason": "Refreshing in this heat."
    }
}

# 6. Meal Time Intelligence
MEAL_TIMES = {
    "breakfast": CATEGORIES["breakfast"],
    "lunch": CATEGORIES["fasting"] + CATEGORIES["meat"] + CATEGORIES["platters"],
    "dinner": ["tibs", "kitfo", "doro wat", "shiro", "salata", "light meals"],
    "snacks": CATEGORIES["snacks"] + CATEGORIES["drinks"]
}

# 7. Popularity Intelligence (Mock weights for scoring)
POPULARITY_MULTIPLIERS = {
    "trending": 1.2,
    "most_reordered": 1.3,
    "highly_rated": 1.1
}

# 8. Ethiopian Vendor Intelligence Categories
VENDOR_INTELLIGENCE = {
    "traditional_restaurants": ["Yod Abyssinia", "2000 Habesha", "Fasika", "Chane"],
    "fasting_focused": ["Vegan options", "Salad bar", "Juice house"],
    "healthy": ["Salad", "Juice", "Smoothies"],
    "budget": ["Local cafeterias", "Street food", "Small diners"]
}
