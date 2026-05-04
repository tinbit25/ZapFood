package com.example.food.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.food.ui.screens.*
import com.example.food.ui.screens.auth.LoginScreen
import com.example.food.ui.screens.auth.SignUpScreen
import com.example.food.ui.screens.auth.SplashScreen
import com.example.food.ui.screens.auth.WelcomeScreen
import com.example.food.ui.screens.cart.CartScreen
import com.example.food.ui.screens.details.ProductDetailsScreen
import com.example.food.ui.screens.home.HomeScreen
import com.example.food.ui.screens.menu.MenuScreen
import com.example.food.ui.screens.profile.ProfileScreen
import com.example.food.ui.screens.search.SearchScreen
import com.example.food.ui.screens.notifications.NotificationScreen
import com.example.food.ui.screens.address.AddressScreen
import com.example.food.ui.screens.orders.OrderHistoryScreen
import com.example.food.ui.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.food.ui.components.BottomNavBar

@Composable
fun AppNavigation(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Menu.route,
        Screen.Cart.route,
        Screen.Profile.route
    )

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
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }
        
        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }
        
        composable(route = Screen.Home.route) {
            HomeScreen(
                userViewModel = userViewModel,
                onNavigateToDetails = { productId ->
                    navController.navigate(Screen.ProductDetails.createRoute(productId))
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
            MenuScreen()
        }
        
        composable(route = Screen.Cart.route) {
            CartScreen()
        }
        
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                userViewModel = userViewModel,
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToOrders = { navController.navigate(Screen.OrderHistory.route) },
                onNavigateToAddresses = { navController.navigate(Screen.Addresses.route) },
                onNavigateToSettings = { /* Add settings route if needed */ }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(route = Screen.ProductDetails.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
}
