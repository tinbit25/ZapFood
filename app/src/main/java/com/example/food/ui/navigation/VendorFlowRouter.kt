package com.example.food.ui.navigation

import com.example.food.ui.viewmodel.VendorUIState

object VendorFlowRouter {
    /**
     * Determines which screen the vendor should see based on their current state.
     */
    fun determineDestination(state: VendorUIState): String {
        return when (state) {
            is VendorUIState.OnboardingRequired -> Screen.VendorRegistration.route
            is VendorUIState.PendingReview, 
            is VendorUIState.Verifying, 
            is VendorUIState.Suspended, 
            is VendorUIState.Rejected -> "vendor_verification_state_screen" // Placeholder for screen logic
            is VendorUIState.Active -> Screen.VendorDashboard.route
            else -> Screen.Home.route
        }
    }
}
