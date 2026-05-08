package com.example.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.food.ui.navigation.AppNavigation
import com.example.food.ui.theme.FoodTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Global flag set when user returns from Chapa checkout via deep link.
         * The PaymentViewModel checks this to auto-verify the payment.
         */
        var pendingPaymentReturn: Boolean = false
            private set

        fun clearPaymentReturn() {
            pendingPaymentReturn = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if launched via deep link (zapfood://payment/return)
        handleDeepLink(intent)

        setContent {
            FoodTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already running
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        Log.i(TAG, "Deep link received: $uri")

        if (uri.scheme == "zapfood" && uri.host == "payment") {
            Log.i(TAG, "Payment return deep link detected — flagging for auto-verify")
            pendingPaymentReturn = true
        }
    }
}