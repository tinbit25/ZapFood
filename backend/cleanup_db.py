import firebase_admin
from firebase_admin import credentials, firestore

def cleanup_database():
    cred = credentials.Certificate("firebase-service-account.json")
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    print("--- Starting Database Cleanup ---")

    # 1. Delete all meals
    meals_ref = db.collection("meals")
    meals = meals_ref.get()
    deleted_meals = 0
    for meal in meals:
        meal.reference.delete()
        deleted_meals += 1
    print(f"Deleted {deleted_meals} meals.")

    # 2. Delete all vendors except 'Tinas'
    # Tinas ID: fdce68d6-cd87-4b8b-81bd-d3ae5e38f5b2
    vendors_ref = db.collection("vendors")
    vendors = vendors_ref.get()
    deleted_vendors = 0
    tinas_id = "fdce68d6-cd87-4b8b-81bd-d3ae5e38f5b2"
    
    for vendor in vendors:
        if vendor.id != tinas_id:
            vendor.reference.delete()
            deleted_vendors += 1
        else:
            print(f"Keeping Vendor with ID: {vendor.id}")

    print(f"Deleted {deleted_vendors} other vendors.")
    print("--- Cleanup Complete ---")

if __name__ == "__main__":
    cleanup_database()
