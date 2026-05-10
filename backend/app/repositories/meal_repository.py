# Facade for fetching meals from Firebase
# Currently the backend accepts candidate meals directly from Android.
# This repository is prepared for the transition to a Stateful Backend.

class MealRepository:
    def get_available_meals(self):
        # TODO: Fetch directly from Firebase
        return []
