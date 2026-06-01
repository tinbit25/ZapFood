"""
fix_meal_images.py
──────────────────
Scans every document in the Firestore `meals` collection and updates
`imageUrl` to a curated, food-specific Unsplash photo whenever the
stored URL is either empty or one of the known wrong seed photos.

Usage:
    cd backend
    .\\venv\\Scripts\\python.exe fix_meal_images.py           # preview only
    .\\venv\\Scripts\\python.exe fix_meal_images.py --apply   # write to Firestore
"""

import sys
import os
import re
import json

# ── Initialise Firebase ────────────────────────────────────────────────────────
try:
    from app.services.firebase_client import initialize_firebase, get_firestore_client
except ImportError as e:
    print("[ERROR] firebase_admin module not found. Please install firebase-admin and configure credentials before running this script.")
    sys.exit(1)

initialize_firebase()
db = get_firestore_client()

# ── Image map: ordered list of (keyword, url) ──────────────────────────────────
# Keywords are matched against the lowercase meal name; first match wins.
KEYWORD_IMAGE_MAP = [
    # Ethiopian staples
    ("kitfo",       "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop"),
    ("tibs",        "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop"),
    ("doro wot",    "https://images.unsplash.com/photo-1598103442097-8b74394b95c8?w=600&auto=format&fit=crop"),
    ("doro wat",    "https://images.unsplash.com/photo-1598103442097-8b74394b95c8?w=600&auto=format&fit=crop"),
    ("shiro",       "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&auto=format&fit=crop"),
    ("misir",       "https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=600&auto=format&fit=crop"),
    ("gomen",       "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    ("beyaynetu",   "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    ("injera",      "https://images.unsplash.com/photo-1567360425618-1594206637d2?w=600&auto=format&fit=crop"),
    ("firfir",      "https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?w=600&auto=format&fit=crop"),
    ("kategna",     "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop"),
    ("chechebsa",   "https://images.unsplash.com/photo-1525351484163-7529414f2626?w=600&auto=format&fit=crop"),
    ("ful",         "https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=600&auto=format&fit=crop"),
    ("asa tibs",    "https://images.unsplash.com/photo-1580959375944-abd7e991f971?w=600&auto=format&fit=crop"),
    ("dulet",       "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop"),
    ("ayib",        "https://images.unsplash.com/photo-1486297678162-eb2a19b0a318?w=600&auto=format&fit=crop"),
    ("teff",        "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop"),
    ("gursha",      "https://images.unsplash.com/photo-1567360425618-1594206637d2?w=600&auto=format&fit=crop"),
    # Protein
    ("chicken",     "https://images.unsplash.com/photo-1598103442097-8b74394b95c8?w=600&auto=format&fit=crop"),
    ("beef",        "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop"),
    ("lamb",        "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&auto=format&fit=crop"),
    ("fish",        "https://images.unsplash.com/photo-1580959375944-abd7e991f971?w=600&auto=format&fit=crop"),
    ("shrimp",      "https://images.unsplash.com/photo-1580959375944-abd7e991f971?w=600&auto=format&fit=crop"),
    ("tuna",        "https://images.unsplash.com/photo-1580959375944-abd7e991f971?w=600&auto=format&fit=crop"),
    # Vegan / fasting
    ("vegan",       "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    ("vegetarian",  "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    ("fasting",     "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    ("lentil",      "https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?w=600&auto=format&fit=crop"),
    ("salad",       "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop"),
    # Western / fast-food
    ("burger",      "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop"),
    ("sandwich",    "https://images.unsplash.com/photo-1626700051175-656868edfab9?w=600&auto=format&fit=crop"),
    ("pizza",       "https://images.unsplash.com/photo-1574071318508-1cdbad80ad50?w=600&auto=format&fit=crop"),
    ("pasta",       "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?w=600&auto=format&fit=crop"),
    ("sushi",       "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop"),
    # Breakfast
    ("breakfast",   "https://images.unsplash.com/photo-1525351484163-7529414f2626?w=600&auto=format&fit=crop"),
    ("egg",         "https://images.unsplash.com/photo-1525351484163-7529414f2626?w=600&auto=format&fit=crop"),
    ("pancake",     "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=600&auto=format&fit=crop"),
    # Drinks / desserts
    ("smoothie",    "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=600&auto=format&fit=crop"),
    ("juice",       "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=600&auto=format&fit=crop"),
    ("coffee",      "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600&auto=format&fit=crop"),
    ("tea",         "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=600&auto=format&fit=crop"),
    ("cake",        "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop"),
    ("soup",        "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&auto=format&fit=crop"),
    ("stew",        "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&auto=format&fit=crop"),
]

# Photo IDs from the app's seed functions that are known to show the wrong food.
WRONG_SEED_PHOTO_IDS = {
    "1568901346375",   # Burger used for Ethiopian dishes
    "1626700051175",   # Crispy chicken sandwich
    "1606787366850",   # Generic
    "1589647363585",   # Incorrectly paired with Shiro
    "1541014741259",   # Incorrectly paired with Injera
    "1512621776951",   # Generic salad
    "1490474418585",   # Smoothie
    "1579871494447",   # Sushi
    "1574071318508",   # Pizza
}

FALLBACK_IMAGES = [
    "https://images.unsplash.com/photo-1567360425618-1594206637d2?w=600&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1504754524776-8f4f37790ca0?w=600&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&auto=format&fit=crop",
]


def resolve_image(meal_name: str, stored_url: str) -> str | None:
    """
    Returns the correct image URL for a meal, or None if the current URL is already fine.
    """
    # Trust Firebase Storage uploads
    if "firebasestorage.googleapis.com" in stored_url:
        return None  # No change needed

    # Check if stored URL contains a wrong seed photo ID
    is_wrong = any(pid in stored_url for pid in WRONG_SEED_PHOTO_IDS)

    # Try keyword match on meal name
    lower = meal_name.lower().strip()
    for keyword, url in KEYWORD_IMAGE_MAP:
        if keyword in lower:
            # Only update if it's wrong or empty
            if is_wrong or not stored_url:
                return url
            # If the stored URL already contains this photo ID, no change needed
            return None

    # No keyword matched
    if is_wrong or not stored_url:
        idx = abs(hash(meal_name)) % len(FALLBACK_IMAGES)
        return FALLBACK_IMAGES[idx]

    return None  # URL looks fine


def main():
    apply = "--apply" in sys.argv
    mode = "APPLY" if apply else "DRY-RUN (preview only — pass --apply to write)"
    print(f"\n{'=' * 60}")
    print(f"  ZapFood Meal Image Fixer  [{mode}]")
    print(f"{'=' * 60}\n")

    docs = db.collection("meals").stream()
    total = 0
    to_fix = 0
    fixed = 0

    for doc in docs:
        total += 1
        data = doc.to_dict() or {}
        name = data.get("name", "")
        stored_url = data.get("imageUrl", "")

        new_url = resolve_image(name, stored_url)
        if new_url:
            to_fix += 1
            print(f"  [{doc.id[:12]}...] \"{name}\"")
            print(f"    OLD: {stored_url[:80] if stored_url else '(empty)'}")
            print(f"    NEW: {new_url[:80]}")
            if apply:
                db.collection("meals").document(doc.id).update({"imageUrl": new_url})
                fixed += 1
                print(f"    ✅ Updated\n")
            else:
                print(f"    ⚠️  Would update (dry-run)\n")
        else:
            print(f"  [OK] \"{name}\" — image looks correct")

    print(f"\n{'=' * 60}")
    print(f"  Total meals   : {total}")
    print(f"  Need fixing   : {to_fix}")
    if apply:
        print(f"  Fixed         : {fixed}")
    else:
        print(f"  Run with --apply to apply changes to Firestore")
    print(f"{'=' * 60}\n")


if __name__ == "__main__":
    main()
