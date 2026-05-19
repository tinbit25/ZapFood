package com.example.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.compose.rememberNavController
import com.example.food.core.util.NotificationChannelManager
import com.example.food.data.remote.FCMTokenManager
import com.example.food.ui.navigation.AppNavigation
import com.example.food.ui.navigation.Screen
import com.example.food.ui.theme.FoodTheme
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.food.data.datastore.AppTheme
import com.example.food.data.datastore.SettingsRepository
import com.example.food.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Global flag set when user returns from Chapa checkout via deep link.
         * The PaymentViewModel checks this to auto-verify the payment.
         */
        var pendingPaymentReturn: Boolean = false
            private set

        var pendingNotificationRoute: String? = null
            private set

        var pendingNotificationId: String? = null
            private set

        fun clearPaymentReturn() {
            pendingPaymentReturn = false
        }

        fun clearNotificationRoute() {
            pendingNotificationRoute = null
        }

        fun clearNotificationId() {
            pendingNotificationId = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Notification Channels
        NotificationChannelManager.createNotificationChannels(this)

        // Sync FCM Token
        FCMTokenManager().syncToken()

        // Ask for notification permissions (required for Android 13+)
        askNotificationPermission()

        // Check if launched via notification or deep link
        handleIntent(intent)

        setContent {
            val settingsRepo = remember { SettingsRepository(applicationContext) }
            val appTheme by settingsRepo.theme.collectAsState(initial = AppTheme.DARK)
            val isDark = ThemeManager.shouldUserDarkTheme(appTheme)
            FoodTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        AppNavigation(navController = navController)
                        
                        com.example.food.ui.components.BroadcastBanner(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link or notification when app is already running
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        // Handle Notification Click
        val type = intent.getStringExtra("notification_type")
        val notificationId = intent.getStringExtra("notification_id")
        
        if (type != null) {
            Log.i(TAG, "Notification clicked: type=$type, id=$notificationId")
            
            // Capture the notification ID if present
            pendingNotificationId = notificationId
            
            // All notification clicks now lead to the Notifications Screen as requested
            pendingNotificationRoute = Screen.Notifications.route
        }

        // Handle Deep Link (zapfood://payment/return)
        val uri = intent.data
        if (uri != null) {
            Log.i(TAG, "Deep link received: $uri")
            if (uri.scheme == "zapfood" && uri.host == "payment") {
                Log.i(TAG, "Payment return deep link detected — flagging for auto-verify")
                pendingPaymentReturn = true
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }
}