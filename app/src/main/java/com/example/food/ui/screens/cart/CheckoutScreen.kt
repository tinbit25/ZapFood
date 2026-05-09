package com.example.food.ui.screens.cart

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.data.model.*
import com.example.food.ui.viewmodel.PaymentViewModel
import com.example.food.ui.viewmodel.PaymentState
import com.example.food.ui.viewmodel.CartViewModel
import com.example.food.ui.viewmodel.OrderViewModel
import com.example.food.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun CheckoutScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    rewardViewModel: com.example.food.ui.viewmodel.RewardViewModel,
    paymentViewModel: PaymentViewModel,
    onNavigateBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val pointsBalance by rewardViewModel.pointsBalance.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    var pointsToRedeem by remember { mutableIntStateOf(0) }
    val rewardDiscount = (pointsToRedeem / 10) * 100.0 // 10 points = 100 ETB
    
    val deliveryFee = 50.0 // ETB
    val subtotal = cartState.subtotal
    val total = subtotal + deliveryFee - rewardDiscount

    val paymentState by paymentViewModel.paymentState.collectAsState()
    
    var selectedAddress by remember { mutableStateOf("Bole, Addis Ababa, Ethiopia") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let { rewardViewModel.fetchBalance(it.userId) }
    }

    // Auto-verify payment when user returns from Chapa checkout via deep link
    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.CheckoutReady &&
            com.example.food.MainActivity.pendingPaymentReturn
        ) {
            com.example.food.MainActivity.clearPaymentReturn()
            paymentViewModel.verifyPayment()
        }
    }

    // Handle payment state changes
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.CheckoutReady -> {
                // Open Chapa checkout in Custom Tabs
                val checkoutUrl = (paymentState as PaymentState.CheckoutReady).checkoutUrl
                openCheckoutInBrowser(context, checkoutUrl)
            }
            is PaymentState.Success -> {
                if (pointsToRedeem > 0) {
                    user?.let { rewardViewModel.redeemPoints(it.userId, pointsToRedeem) }
                }
                cartViewModel.clearCart()
                val orderId = (paymentState as PaymentState.Success).payment.orderId
                paymentViewModel.resetState()
                onOrderSuccess(orderId)
            }
            is PaymentState.Error -> {
                val message = (paymentState as PaymentState.Error).message
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopNavBar(title = "Checkout", onBackClick = onNavigateBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Delivery Address", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                CheckoutOptionCard(
                    title = selectedAddress,
                    icon = Icons.Default.LocationOn,
                    onClick = { /* Navigate to address selection */ }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Reward Points Section
                if (pointsBalance > 0) {
                    Text(text = "Reward Points", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1A1A1A)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "You have $pointsBalance points", color = Color.White, fontSize = 14.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = { 
                                    pointsToRedeem = if (pointsToRedeem > 0) 0 else pointsBalance 
                                }) {
                                    Text(text = if (pointsToRedeem > 0) "Remove" else "Redeem All", color = Color(0xFFF16B24))
                                }
                            }
                            if (pointsToRedeem > 0) {
                                Text(text = "- ETB ${rewardDiscount.toInt()} discount applied", color = Color(0xFF4CAF50), fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                Text(text = "Payment Method", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethodChip(
                        method = PaymentMethod.CARD,
                        isSelected = selectedMethod == PaymentMethod.CARD,
                        onClick = { selectedMethod = PaymentMethod.CARD }
                    )
                    PaymentMethodChip(
                        method = PaymentMethod.MOBILE_MONEY,
                        isSelected = selectedMethod == PaymentMethod.MOBILE_MONEY,
                        onClick = { selectedMethod = PaymentMethod.MOBILE_MONEY }
                    )
                    PaymentMethodChip(
                        method = PaymentMethod.CASH,
                        isSelected = selectedMethod == PaymentMethod.CASH,
                        onClick = { selectedMethod = PaymentMethod.CASH }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Order Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    for (pair in cartState.mealPlans) {
                        val plan = pair.first
                        val qty = pair.second
                        SummaryRow(plan.name, "x$qty", "ETB ${"%,.0f".format(plan.price * qty)}")
                    }
                    for (pair in cartState.meals) {
                        val meal = pair.first
                        val qty = pair.second
                        SummaryRow(meal.name, "x$qty", "ETB ${"%,.0f".format(meal.price * qty)}")
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.3f))
                    
                    SummaryRow("Subtotal", "", "ETB ${"%,.0f".format(subtotal)}")
                    SummaryRow("Delivery Fee", "", "ETB ${"%,.0f".format(deliveryFee)}")
                    if (rewardDiscount > 0) {
                        SummaryRow("Reward Discount", "", "- ETB ${"%,.0f".format(rewardDiscount)}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow("Total", "", "ETB ${"%,.0f".format(total)}", isBold = true)
                }
            }

            // Bottom action area
            Box(modifier = Modifier.padding(24.dp)) {
                Column {
                    PrimaryButton(
                        text = when (paymentState) {
                            is PaymentState.Loading -> "Initializing Payment..."
                            is PaymentState.Verifying -> "Verifying Payment..."
                            is PaymentState.CheckoutReady -> "Opening Checkout..."
                            else -> if (isPlacingOrder) "Placing Order..." else "Confirm Order"
                        },
                        enabled = !isPlacingOrder
                                && paymentState !is PaymentState.Loading
                                && paymentState !is PaymentState.Verifying
                                && (cartState.meals.isNotEmpty() || cartState.mealPlans.isNotEmpty()),
                        onClick = {
                            val currentUser = user
                            if (currentUser == null) {
                                scope.launch { snackbarHostState.showSnackbar("User not authenticated") }
                                return@PrimaryButton
                            }

                            // If we're in CheckoutReady, user came back — verify payment
                            if (paymentState is PaymentState.CheckoutReady) {
                                paymentViewModel.verifyPayment()
                                return@PrimaryButton
                            }

                            isPlacingOrder = true
                            val allMealIds = mutableListOf<String>()
                            for (pair in cartState.meals) {
                                val meal = pair.first
                                val qty = pair.second
                                repeat(qty) { allMealIds.add(meal.id) }
                            }
                            val planId = cartState.mealPlans.firstOrNull()?.first?.id

                            orderViewModel.placeOrder(currentUser, allMealIds, planId) { resource ->
                                isPlacingOrder = false
                                when (resource) {
                                    is Resource.Success -> {
                                        val order = resource.data!!
                                        if (selectedMethod == PaymentMethod.CASH) {
                                            cartViewModel.clearCart()
                                            onOrderSuccess(order.orderId)
                                        } else {
                                            // Initiate Chapa payment via backend
                                            paymentViewModel.initiatePayment(
                                                orderId = order.orderId,
                                                userId = currentUser.userId,
                                                amount = total,
                                                method = selectedMethod
                                            )
                                        }
                                    }
                                    is Resource.Error -> {
                                        scope.launch { snackbarHostState.showSnackbar(resource.message ?: "Failed to place order") }
                                    }
                                    else -> {}
                                }
                            }
                        },
                        backgroundColor = Color(0xFFF16B24)
                    )

                    // Show "Verify Payment" button when user returns from checkout
                    if (paymentState is PaymentState.CheckoutReady) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "I've Completed Payment",
                            enabled = true,
                            onClick = { paymentViewModel.verifyPayment() },
                            backgroundColor = Color(0xFF4CAF50)
                        )
                    }

                    if (paymentState is PaymentState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (paymentState as PaymentState.Error).message,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Opens the Chapa checkout URL in Chrome Custom Tabs.
 * Falls back to regular browser if Custom Tabs is not available.
 */
private fun openCheckoutInBrowser(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback: open in default browser
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e2: Exception) {
            android.util.Log.e("CheckoutScreen", "Cannot open checkout URL", e2)
        }
    }
}

@Composable
fun CheckoutOptionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFFF16B24))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f))
            Text(text = "Change", color = Color(0xFFF16B24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryRow(label: String, qty: String, price: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text(
                text = label, 
                fontSize = 14.sp, 
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = if (isBold) Color.White else Color.Gray
            )
            if (qty.isNotEmpty()) {
                Text(text = " $qty", fontSize = 14.sp, color = Color.Gray)
            }
        }
        Text(
            text = price, 
            fontSize = 14.sp, 
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isBold) Color(0xFFF16B24) else Color.White
        )
    }
}

@Composable
fun PaymentMethodChip(method: PaymentMethod, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFFF16B24) else Color(0xFF1A1A1A),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Text(
            text = method.name.replace("_", " "),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}
