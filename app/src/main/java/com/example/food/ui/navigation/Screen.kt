package com.example.food.ui.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    object Splash : Screen("splash_screen")
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    // Main App Flow
    object Home : Screen("home_screen")
    object Menu : Screen("menu_screen")
    object Cart : Screen("cart_screen")
    object Profile : Screen("profile_screen")
    object Search : Screen("search_screen")
    object Notifications : Screen("notifications_screen")
    object Addresses : Screen("addresses_screen")
    object OrderHistory : Screen("order_history_screen")
    
    // Details
    object ProductDetails : Screen("product_details_screen/{productId}") {
        fun createRoute(productId: String) = "product_details_screen/$productId"
    }
}
