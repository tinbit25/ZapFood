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
import com.example.food.ui.screens.auth.PhoneLoginScreen
import com.example.food.ui.screens.auth.OTPVerificationScreen
import com.example.food.ui.screens.auth.AuthViewModel
import com.example.food.ui.screens.profile.ProfileScreen
import com.example.food.ui.screens.profile.ProfileEditScreen
import com.example.food.ui.screens.search.SearchScreen
import com.example.food.ui.screens.notifications.NotificationScreen
import com.example.food.ui.screens.address.AddressScreen
import com.example.food.ui.screens.address.AddressFormScreen
import com.example.food.ui.screens.orders.OrderHistoryScreen
import com.example.food.ui.screens.orders.OrderTrackingScreen
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.ui.viewmodel.MealPlanViewModel
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.SearchViewModel
import com.example.food.ui.viewmodel.RecommendationViewModel
import com.example.food.data.repository.RecentSearchRepository
import com.example.food.ui.screens.search.GlobalSearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.ui.screens.auth.PreferencesOnboardingScreen
import com.example.food.ui.screens.menu.SmartPreferenceScreen
import com.example.food.ui.screens.menu.CustomPlanCreatorScreen
import com.example.food.ui.screens.checkout.CheckoutScreen
import com.example.food.ui.screens.cart.OrderSuccessScreen
import com.example.food.ui.screens.details.MealPlanDetailsScreen
import com.example.food.ui.screens.vendor.VendorDashboardScreen
import com.example.food.ui.screens.vendor.VendorOrdersScreen
import com.example.food.ui.screens.vendor.VendorMenuManagementScreen
import com.example.food.ui.screens.vendor.VendorAnalyticsScreen
import com.example.food.ui.screens.vendor.VendorStoreScreen
import com.example.food.ui.screens.vendor.VendorPickupScannerScreen
import com.example.food.ui.screens.support.SupportTicketScreen
import com.example.food.ui.screens.admin.AdminSupportDashboardScreen
import com.example.food.ui.screens.feedback.FeedbackScreen
import com.example.food.ui.screens.onboarding.OnboardingScreen
import com.example.food.ui.viewmodel.OnboardingViewModel
import com.example.food.data.datastore.OnboardingDataStore
import com.example.food.ui.screens.settings.SettingsScreen
import com.example.food.ui.screens.settings.NotificationSettingsScreen
import com.example.food.ui.viewmodel.SettingsViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.getInstance
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.food.ui.components.BottomNavBar
import com.example.food.ui.screens.vendor.VendorDiscoveryScreen
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
    paymentViewModel: com.example.food.ui.viewmodel.PaymentViewModel = viewModel(),
    recommendationViewModel: RecommendationViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    addressViewModel: com.example.food.ui.viewmodel.AddressViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    vendorStateManager: com.example.food.ui.viewmodel.VendorStateManager = viewModel(),
    smartTableViewModel: com.example.food.ui.viewmodel.SmartTableViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val user by userViewModel.user.collectAsState()
    val userRole = user?.role ?: com.example.food.data.model.UserRole.CUSTOMER

    val customerBottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Menu.route,
        Screen.Cart.route,
        Screen.Profile.route
    )
    val vendorBottomBarRoutes = listOf(
        Screen.VendorDashboard.route,
        Screen.VendorOrders.route,
        Screen.VendorMenu.route,
        Screen.VendorAnalytics.route,
        Screen.VendorStore.route
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

    // REACTIVE VENDOR REDIRECT — fixes the Firestore race condition.
    // The splash navigates before user data loads. Once the user flow emits,
    // we check the role and redirect vendors to their Merchant OS immediately.
    LaunchedEffect(user) {
        val loadedUser = user ?: return@LaunchedEffect
        val isOnCustomerScreen = currentRoute in customerBottomBarRoutes
        val isOnSplash = currentRoute == Screen.Splash.route
        if (loadedUser.role == com.example.food.data.model.UserRole.VENDOR && (isOnCustomerScreen || isOnSplash)) {
            navController.navigate(Screen.VendorDashboard.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = if (userRole == com.example.food.data.model.UserRole.VENDOR) androidx.compose.ui.graphics.Color(0xFF0F0F0F) else androidx.compose.ui.graphics.Color.Unspecified,
        bottomBar = {
            if (userRole == com.example.food.data.model.UserRole.VENDOR) {
                // Vendor Merchant OS Navigation
                if (currentRoute in vendorBottomBarRoutes) {
                    com.example.food.ui.components.VendorBottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.VendorDashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            } else {
                // Customer Navigation
                if (currentRoute in customerBottomBarRoutes) {
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
                        // Always navigate to Home; the reactive vendor redirect LaunchedEffect
                        // in AppNavigation will intercept and send vendors to VendorDashboard
                        // once the Firestore user data loads (fixes the async race condition).
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToWelcome = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.Onboarding.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return OnboardingViewModel(OnboardingDataStore(context)) as T
                        }
                    }
                )
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onNavigateToWelcome = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Welcome.route) {
                WelcomeScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToPhone = { navController.navigate(Screen.PhoneLogin.createRoute(false)) }
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
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onNavigateToPhone = { navController.navigate(Screen.PhoneLogin.createRoute(false)) }
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
                    onNavigateToOnboarding = { role ->
                        if (role == com.example.food.data.model.UserRole.VENDOR) {
                            navController.navigate(Screen.VendorRegistration.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.PreferencesOnboarding.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            
            composable(
                route = Screen.PhoneLogin.route,
                arguments = listOf(navArgument("isLinking") { type = NavType.BoolType })
            ) { backStackEntry ->
                val isLinking = backStackEntry.arguments?.getBoolean("isLinking") ?: false
                PhoneLoginScreen(
                    onNavigateToOTP = { phone, linking ->
                        navController.navigate(Screen.OTPVerification.createRoute(phone, linking))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    isLinking = isLinking,
                    viewModel = authViewModel
                )
            }

            composable(
                route = Screen.OTPVerification.route,
                arguments = listOf(
                    navArgument("phone") { type = NavType.StringType },
                    navArgument("isLinking") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone") ?: ""
                val isLinking = backStackEntry.arguments?.getBoolean("isLinking") ?: false
                OTPVerificationScreen(
                    phoneNumber = phone,
                    isLinking = isLinking,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = authViewModel
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
                    recommendationViewModel = recommendationViewModel,
                    onNavigateToDetails = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onNavigateToMealPlanDetails = { planId ->
                        navController.navigate(Screen.MealPlanDetails.createRoute(planId))
                    },
                    onNavigateToSmartPreference = {
                        navController.navigate(Screen.SmartPreference.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToVendorDiscovery = {
                        navController.navigate(Screen.VendorDiscovery.route)
                    },
                    onNavigateToTableScan = {
                        navController.navigate(Screen.SmartTableScan.route)
                    },
                    smartTableViewModel = smartTableViewModel
                )
            }
            
            composable(route = Screen.Menu.route) {
                MenuScreen(
                    onNavigateToAI = { navController.navigate(Screen.SmartPreference.route) },
                    onNavigateToCustom = { navController.navigate(Screen.CustomPlanCreator.route) },
                    onNavigateToBrowse = { navController.navigate(Screen.Home.route) }
                )
            }
            
            composable(route = Screen.SmartPreference.route) {
                com.example.food.ui.screens.menu.SmartPreferenceScreen(
                    recommendationViewModel = recommendationViewModel,
                    userViewModel = userViewModel,
                    onPreferencesSaved = {
                        navController.popBackStack()
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
                    recommendationViewModel = recommendationViewModel,
                    onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                )
            }

            composable(route = Screen.Checkout.route) {
                val rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel = viewModel()
                val checkoutViewModel: com.example.food.ui.viewmodel.CheckoutViewModel = viewModel()
                CheckoutScreen(
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    cartViewModel = cartViewModel,
                    rewardViewModel = rewardViewModel,
                    paymentViewModel = paymentViewModel,
                    checkoutViewModel = checkoutViewModel,
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
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToVendorDashboard = { navController.navigate(Screen.VendorDashboard.route) },
                    onNavigateToVendorMenu = { navController.navigate(Screen.VendorMenu.route) },
                    onNavigateToSupportTickets = { navController.navigate(Screen.SupportTickets.route) },
                    onNavigateToAdminSupport = { navController.navigate(Screen.AdminSupportDashboard.route) },
                    onNavigateToLinkPhone = {
                        navController.navigate(Screen.PhoneLogin.createRoute(true))
                    },
                    onNavigateToVendorRegistration = {
                        navController.navigate(Screen.VendorRegistration.route)
                    }
                )
            }

            composable(route = Screen.ProfileEdit.route) {
                com.example.food.ui.screens.profile.AdvancedProfileEditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel
                )
            }


            composable(route = Screen.Notifications.route) {
                NotificationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.Addresses.route) {
                AddressScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddAddress = { navController.navigate(Screen.AddressAdd.route) },
                    onNavigateToEditAddress = { id -> navController.navigate(Screen.AddressEdit.createRoute(id)) },
                    userViewModel = userViewModel,
                    addressViewModel = addressViewModel
                )
            }

            composable(route = Screen.AddressAdd.route) {
                AddressFormScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel,
                    addressViewModel = addressViewModel
                )
            }

            composable(
                route = Screen.AddressEdit.route,
                arguments = listOf(navArgument("addressId") { type = NavType.StringType })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getString("addressId") ?: ""
                AddressFormScreen(
                    addressId = addressId,
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel,
                    addressViewModel = addressViewModel
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
                val vendorUIState by vendorStateManager.uiState.collectAsState()

                LaunchedEffect(user?.userId) {
                    vendorStateManager.startObserving(user?.userId)
                }

                com.example.food.ui.components.VendorStateGuard(
                    state = vendorUIState,
                    onActive = {
                        VendorDashboardScreen(
                            userViewModel = userViewModel,
                            orderViewModel = orderViewModel,
                            onNavigateToScan = { navController.navigate(Screen.VendorPickupScan.route) },
                            onNavigateToAddMeal = { navController.navigate(Screen.VendorMenu.route) }
                        )
                    },
                    onPending = {
                        com.example.food.ui.screens.vendor.VendorVerificationStateScreen(
                            state = vendorUIState,
                            onNavigateBack = { navController.popBackStack() },
                            onContactSupport = { navController.navigate(Screen.SupportTickets.route) }
                        )
                    },
                    onOnboarding = {
                        com.example.food.ui.screens.vendor.VendorRegistrationScreen(
                            userId = user?.userId ?: "",
                            onNavigateBack = { navController.popBackStack() },
                            onRegistrationSuccess = {}
                        )
                    },
                    onRestricted = { restrictedState ->
                        com.example.food.ui.screens.vendor.VendorVerificationStateScreen(
                            state = restrictedState,
                            onNavigateBack = { navController.popBackStack() },
                            onContactSupport = { navController.navigate(Screen.SupportTickets.route) }
                        )
                    }
                )
            }

            composable(route = Screen.VendorOrders.route) {
                VendorOrdersScreen(
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel
                )
            }

            composable(route = Screen.VendorMenu.route) {
                VendorMenuManagementScreen(
                    userViewModel = userViewModel,
                    mealViewModel = mealViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(route = Screen.VendorAnalytics.route) {
                VendorAnalyticsScreen()
            }

            composable(route = Screen.VendorStore.route) {
                VendorStoreScreen(
                    userViewModel = userViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.VendorPickupScan.route) {
                VendorPickupScannerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel
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
                    onNavigateBack = { navController.popBackStack() },
                    onShowQRCode = { id -> navController.navigate(Screen.PickupQRCode.createRoute(id)) },
                    onNavigateToTableScan = { navController.navigate(Screen.SmartTableScan.route) }
                )
            }
            composable(
                route = Screen.PickupQRCode.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.example.food.ui.screens.orders.PickupQRCodeScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.QRPayment.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                com.example.food.ui.screens.payment.QRPaymentScreen(
                    orderId = orderId,
                    onNavigateBack = { navController.popBackStack() },
                    onPaymentSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                    onLogout = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    settingsViewModel = settingsViewModel,
                    userViewModel = userViewModel
                )
            }
            composable(route = Screen.Search.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val searchViewModel: SearchViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return SearchViewModel(
                                recentSearchRepository = RecentSearchRepository(context)
                            ) as T
                        }
                    }
                )
                GlobalSearchScreen(
                    onNavigateToMeal = { mealId -> 
                        navController.navigate(Screen.ProductDetails.createRoute(mealId))
                    },
                    onNavigateToVendor = { vendorId -> 
                        navController.navigate(Screen.VendorStorefront.createRoute(vendorId))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = searchViewModel
                )
            }

            composable(route = Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    settingsViewModel = settingsViewModel
                )
            }
            composable(route = Screen.VendorDiscovery.route) {
                VendorDiscoveryScreen(
                    onNavigateToVendor = { vendorId ->
                        navController.navigate(Screen.VendorStorefront.createRoute(vendorId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.VendorStorefront.route,
                arguments = listOf(navArgument("vendorId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
                com.example.food.ui.screens.vendor.VendorStorefrontScreen(
                    vendorId = vendorId,
                    onNavigateBack = { navController.popBackStack() },
                    onMealClick = { mealId ->
                        navController.navigate(Screen.ProductDetails.createRoute(mealId))
                    }
                )
            }

            composable(route = Screen.VendorRegistration.route) {
                val user by userViewModel.user.collectAsState()
                com.example.food.ui.screens.vendor.VendorRegistrationScreen(
                    userId = user?.userId ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.VendorRegistration.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = Screen.SmartTableScan.route) {
                com.example.food.ui.screens.orders.SmartTableScannerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSessionStarted = {
                        navController.navigate(Screen.SmartTableSession.route) {
                            popUpTo(Screen.SmartTableScan.route) { inclusive = true }
                        }
                    },
                    viewModel = smartTableViewModel,
                    userViewModel = userViewModel
                )
            }

            composable(route = Screen.SmartTableSession.route) {
                com.example.food.ui.screens.orders.SmartTableSessionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMenu = { vendorId ->
                        navController.navigate(Screen.VendorStorefront.createRoute(vendorId))
                    },
                    viewModel = smartTableViewModel
                )
            }
        }
    }
}
