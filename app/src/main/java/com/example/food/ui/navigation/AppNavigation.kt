package com.example.food.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.food.ui.screens.*
import com.example.food.ui.screens.auth.LoginScreen
import com.example.food.ui.screens.auth.SignUpScreen
import com.example.food.ui.screens.auth.SplashScreen
import com.example.food.ui.screens.auth.WelcomeScreen
import com.example.food.ui.screens.auth.ForgotPasswordScreen
import com.example.food.ui.screens.auth.ResetPasswordScreen
import com.example.food.ui.screens.cart.CartScreen
import com.example.food.ui.screens.details.ProductDetailsScreen
import com.example.food.ui.screens.home.HomeScreen
import com.example.food.ui.screens.menu.MenuScreen
import com.example.food.ui.screens.profile.ProfileScreen
import com.example.food.ui.screens.profile.ProfileEditScreen
import com.example.food.ui.screens.search.SearchScreen
import com.example.food.ui.screens.notifications.NotificationScreen
import com.example.food.ui.screens.address.AddressScreen
import com.example.food.ui.screens.orders.OrderHistoryScreen
import com.example.food.ui.screens.orders.OrderTrackingScreen
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.ui.viewmodel.CartViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.screens.auth.PreferencesOnboardingScreen
import com.example.food.ui.screens.menu.AIPlanGeneratorScreen
import com.example.food.ui.screens.menu.CustomPlanCreatorScreen
import com.example.food.ui.screens.cart.CheckoutScreen
import com.example.food.ui.screens.cart.OrderSuccessScreen
import com.example.food.ui.screens.details.MealPlanDetailsScreen
import com.example.food.ui.screens.vendor.VendorDashboardScreen
import com.example.food.ui.screens.support.SupportTicketScreen
import com.example.food.ui.screens.admin.AdminSupportDashboardScreen
import com.example.food.ui.screens.feedback.FeedbackScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.food.ui.components.BottomNavBar
import androidx.compose.runtime.LaunchedEffect
import com.example.food.MainActivity

@Composable
fun AppNavigation(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel(),
    mealPlanViewModel: MealPlanViewModel = viewModel(),
    mealViewModel: com.example.food.ui.viewmodel.MealViewModel = viewModel(),
    orderViewModel: com.example.food.ui.viewmodel.OrderViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    paymentViewModel: com.example.food.ui.viewmodel.PaymentViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Menu.route,
        Screen.Cart.route,
        Screen.Profile.route
    )

    // Handle Notification Navigation
    LaunchedEffect(MainActivity.pendingNotificationRoute) {
        MainActivity.pendingNotificationRoute?.let { route ->
            navController.navigate(route) {
                // Ensure we don't build up a large stack
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            MainActivity.clearNotificationRoute()
            // Note: notificationId clearing is handled within NotificationScreen
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
        
            composable(route = Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToWelcome = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
                )
            }
            
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(route = Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.ResetPassword.route) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                ResetPasswordScreen(
                    token = token,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.PreferencesOnboarding.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            
            composable(route = Screen.PreferencesOnboarding.route) {
                PreferencesOnboardingScreen(
                    onComplete = { goals, diets ->
                        // Save to ViewModel and navigate home
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PreferencesOnboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Home.route) {
                HomeScreen(
                    userViewModel = userViewModel,
                    mealPlanViewModel = mealPlanViewModel,
                    mealViewModel = mealViewModel,
                    onNavigateToDetails = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onNavigateToMealPlanDetails = { planId ->
                        navController.navigate(Screen.MealPlanDetails.createRoute(planId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }
            
            composable(route = Screen.Menu.route) {
                MenuScreen(
                    onNavigateToAI = { navController.navigate(Screen.AIPlanGenerator.route) },
                    onNavigateToCustom = { navController.navigate(Screen.CustomPlanCreator.route) },
                    onNavigateToBrowse = { navController.navigate(Screen.Home.route) }
                )
            }
            
            composable(route = Screen.AIPlanGenerator.route) {
                AIPlanGeneratorScreen(
                    mealPlanViewModel = mealPlanViewModel,
                    onPlanGenerated = { planId ->
                        navController.navigate(Screen.MealPlanDetails.createRoute(planId)) {
                            popUpTo(Screen.AIPlanGenerator.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.CustomPlanCreator.route) {
                CustomPlanCreatorScreen(
                    mealViewModel = mealViewModel,
                    mealPlanViewModel = mealPlanViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPlanCreated = {
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(Screen.CustomPlanCreator.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Cart.route) {
                CartScreen(
                    cartViewModel = cartViewModel,
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(route = Screen.Checkout.route) {
                val rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel = viewModel()
                CheckoutScreen(
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    cartViewModel = cartViewModel,
                    rewardViewModel = rewardViewModel,
                    paymentViewModel = paymentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOrderSuccess = { orderId ->
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderSuccessScreen(
                    orderId = orderId,
                    onGoToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onViewOrders = {
                        navController.navigate(Screen.OrderHistory.route)
                    },
                    onTrackOrder = { id ->
                        navController.navigate(Screen.OrderTracking.createRoute(id))
                    }
                )
            }
            
            composable(route = Screen.Profile.route) {
                val rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel = viewModel()
                ProfileScreen(
                    userViewModel = userViewModel,
                    rewardViewModel = rewardViewModel,
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToEdit = { navController.navigate(Screen.ProfileEdit.route) },
                    onNavigateToOrders = { navController.navigate(Screen.OrderHistory.route) },
                    onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                    onNavigateToSettings = { /* Add settings route if needed */ },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToVendorDashboard = { navController.navigate(Screen.VendorDashboard.route) },
                    onNavigateToVendorMenu = { navController.navigate(Screen.VendorMenuManagement.route) },
                    onNavigateToSupportTickets = { navController.navigate(Screen.SupportTickets.route) },
                    onNavigateToAdminSupport = { navController.navigate(Screen.AdminSupportDashboard.route) }
                )
            }

            composable(route = Screen.ProfileEdit.route) {
                ProfileEditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }

            composable(route = Screen.Search.route) {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetails = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    }
                )
            }

            composable(route = Screen.Notifications.route) {
                NotificationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.Addresses.route) {
                AddressScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.OrderHistory.route) {
                OrderHistoryScreen(
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    paymentViewModel = paymentViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFeedback = { orderId ->
                        navController.navigate(Screen.Feedback.createRoute(orderId))
                    },
                    onNavigateToTracking = { orderId ->
                        navController.navigate(Screen.OrderTracking.createRoute(orderId))
                    }
                )
            }
            
            composable(route = Screen.ProductDetails.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailsScreen(
                    productId = productId,
                    cartViewModel = cartViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.MealPlanDetails.route,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: ""
                val rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel = viewModel()
                MealPlanDetailsScreen(
                    planId = planId,
                    mealPlanViewModel = mealPlanViewModel,
                    mealViewModel = mealViewModel,
                    userViewModel = userViewModel,
                    cartViewModel = cartViewModel,
                    rewardViewModel = rewardViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.VendorDashboard.route) {
                VendorDashboardScreen(
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.VendorMenuManagement.route) {
                // For now, navigating to a placeholder or a screen we will create
                com.example.food.ui.screens.vendor.VendorMenuManagementScreen(
                    userViewModel = userViewModel,
                    mealViewModel = mealViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.AdminDashboard.route) {
                val adminViewModel: com.example.food.ui.viewmodel.AdminViewModel = viewModel()
                com.example.food.ui.screens.admin.AdminDashboardScreen(
                    onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                    onNavigateToVendors = { navController.navigate(Screen.AdminVendorManagement.route) },
                    onNavigateToOrders = { navController.navigate(Screen.AdminOrderMonitoring.route) },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = adminViewModel,
                    mealViewModel = mealViewModel,
                    mealPlanViewModel = mealPlanViewModel
                )
            }

            composable(route = Screen.AdminUserManagement.route) {
                val adminViewModel: com.example.food.ui.viewmodel.AdminViewModel = viewModel()
                com.example.food.ui.screens.admin.UserManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = adminViewModel
                )
            }

            composable(route = Screen.AdminVendorManagement.route) {
                val adminViewModel: com.example.food.ui.viewmodel.AdminViewModel = viewModel()
                com.example.food.ui.screens.admin.VendorManagementScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = adminViewModel
                )
            }

            composable(route = Screen.AdminOrderMonitoring.route) {
                val adminViewModel: com.example.food.ui.viewmodel.AdminViewModel = viewModel()
                com.example.food.ui.screens.admin.AdminOrderMonitoringScreen(
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = adminViewModel
                )
            }

            composable(route = Screen.SupportTickets.route) {
                SupportTicketScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }

            composable(route = Screen.AdminSupportDashboard.route) {
                AdminSupportDashboardScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }

            composable(
                route = Screen.Feedback.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId")?.takeIf { it != "none" }
                FeedbackScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }
            composable(
                route = Screen.OrderTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderTrackingScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
