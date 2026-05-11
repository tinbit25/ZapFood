package com.example.food.ui.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    object Onboarding : Screen("onboarding_screen")
    object Splash : Screen("splash_screen")
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object PreferencesOnboarding : Screen("preferences_onboarding_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object ResetPassword : Screen("reset_password_screen/{token}") {
        fun createRoute(token: String) = "reset_password_screen/$token"
    }
    object PhoneLogin : Screen("phone_login_screen/{isLinking}") {
        fun createRoute(isLinking: Boolean) = "phone_login_screen/$isLinking"
    }
    object OTPVerification : Screen("otp_verification_screen/{phone}/{isLinking}") {
        fun createRoute(phone: String, isLinking: Boolean) = "otp_verification_screen/$phone/$isLinking"
    }

    // Main App Flow
    object Home : Screen("home_screen")
    object Menu : Screen("menu_screen")
    object Cart : Screen("cart_screen")
    object Checkout : Screen("checkout_screen")
    object OrderSuccess : Screen("order_success_screen/{orderId}") {
        fun createRoute(orderId: String) = "order_success_screen/$orderId"
    }
    object Profile : Screen("profile_screen")
    object ProfileEdit : Screen("profile_edit_screen")
    object Search : Screen("search_screen")
    object Notifications : Screen("notifications_screen")
    object Addresses : Screen("addresses_screen")
    object AddressAdd : Screen("address_add_screen")
    object AddressEdit : Screen("address_edit_screen/{addressId}") {
        fun createRoute(addressId: String) = "address_edit_screen/$addressId"
    }
    object OrderHistory : Screen("order_history_screen")
    object SmartPreference : Screen("smart_preference_screen")
    object CustomPlanCreator : Screen("custom_plan_creator_screen")
    object OrderTracking : Screen("order_tracking_screen/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking_screen/$orderId"
    }
    
    // Details
    object ProductDetails : Screen("product_details_screen/{productId}") {
        fun createRoute(productId: String) = "product_details_screen/$productId"
    }
    object MealPlanDetails : Screen("meal_plan_details/{planId}") {
        fun createRoute(planId: String) = "meal_plan_details/$planId"
    }

    // Role-based Dashboards
    object VendorDashboard : Screen("vendor_dashboard_screen")
    object VendorMenuManagement : Screen("vendor_menu_management_screen")
    object AdminDashboard : Screen("admin_dashboard_screen")

    object AdminUserManagement : Screen("admin_user_management_screen")
    object AdminVendorManagement : Screen("admin_vendor_management_screen")
    object AdminOrderMonitoring : Screen("admin_order_monitoring_screen")

    // Support and Feedback
    object SupportTickets : Screen("support_tickets_screen")
    object Feedback : Screen("feedback_screen/{orderId}") {
        fun createRoute(orderId: String?) = if (orderId != null) "feedback_screen/$orderId" else "feedback_screen/none"
    }
    object AdminSupportDashboard : Screen("admin_support_dashboard_screen")
}
