# Ethiopian Combo Knowledge Base
# Maps target meal substrings to ideal add-on substrings

ETHIOPIAN_COMBO_RULES = {
    "kitfo": {
        "addons": ["ayib", "kocho", "gomen", "soft drink", "water"],
        "reason": "Perfectly complements Kitfo."
    },
    "shiro": {
        "addons": ["injera", "avocado salad", "lentil soup", "salata", "timatim"],
        "reason": "A great addition to your Shiro."
    },
    "tibs": {
        "addons": ["injera", "awaze", "soft drink", "beer", "shekla"],
        "reason": "Pairs excellently with Tibs."
    },
    "doro": {
        "addons": ["injera", "boiled egg", "soft drink"],
        "reason": "Completes your Doro Wat experience."
    },
    "firfir": {
        "addons": ["tea", "coffee", "boha", "egg"],
        "reason": "A classic morning combination."
    },
    "chechebsa": {
        "addons": ["coffee", "tea", "honey", "yogurt"],
        "reason": "Perfect alongside Chechebsa."
    },
    "ful": {
        "addons": ["bread", "tea", "egg"],
        "reason": "The standard Ful combo."
    }
}

CONTEXTUAL_RULES = {
    "Rainy": {
        "boost_tags": ["soup", "hot", "tea", "atmit", "coffee"],
        "reason": "Perfect for this rainy weather."
    },
    "Hot": {
        "boost_tags": ["salad", "cold drink", "soft drink", "water", "juice"],
        "reason": "Refreshing for a hot day."
    }
}
