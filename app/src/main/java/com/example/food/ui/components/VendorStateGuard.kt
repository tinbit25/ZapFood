package com.example.food.ui.components

import androidx.compose.runtime.Composable
import com.example.food.ui.viewmodel.VendorUIState

@Composable
fun VendorStateGuard(
    state: VendorUIState,
    onActive: @Composable () -> Unit,
    onPending: @Composable () -> Unit,
    onOnboarding: @Composable () -> Unit,
    onRestricted: @Composable (VendorUIState) -> Unit
) {
    when (state) {
        is VendorUIState.Active -> onActive()
        is VendorUIState.PendingReview, is VendorUIState.Verifying -> onPending()
        is VendorUIState.OnboardingRequired -> onOnboarding()
        else -> onRestricted(state)
    }
}
