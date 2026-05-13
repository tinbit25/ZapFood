package com.example.food.ui.screens.checkout

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.food.data.model.*
import com.example.food.ui.components.OrderTypeSelector
import com.example.food.ui.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CheckoutScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    rewardViewModel: RewardViewModel,
    paymentViewModel: PaymentViewModel,
    checkoutViewModel: CheckoutViewModel,
    onNavigateBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val uiState by checkoutViewModel.uiState.collectAsState()
    val isPlacingOrder by checkoutViewModel.isPlacingOrder.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val pointsBalance by rewardViewModel.pointsBalance.collectAsState()
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val rewardDiscount = (checkoutViewModel.pointsToRedeem / 10) * 100.0
    val deliveryFee = if (uiState.orderType == OrderType.DELIVERY) 50.0 else 0.0
    val subtotal = cartState.subtotal
    val total = subtotal + deliveryFee - rewardDiscount

    LaunchedEffect(user) {
        user?.let { rewardViewModel.fetchBalance(it.userId) }
    }

    // Payment Verification Logic
    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.CheckoutReady && com.example.food.MainActivity.pendingPaymentReturn) {
            com.example.food.MainActivity.clearPaymentReturn()
            paymentViewModel.verifyPayment()
        }
    }

    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentState.CheckoutReady -> {
                openCheckoutInBrowser(context, (paymentState as PaymentState.CheckoutReady).checkoutUrl)
            }
            is PaymentState.Success -> {
                if (checkoutViewModel.pointsToRedeem > 0) {
                    user?.let { rewardViewModel.redeemPoints(it.userId, checkoutViewModel.pointsToRedeem) }
                }
                cartViewModel.clearCart()
                val orderId = (paymentState as PaymentState.Success).payment.orderId
                paymentViewModel.resetState()
                onOrderSuccess(orderId)
            }
            is PaymentState.Error -> {
                scope.launch { snackbarHostState.showSnackbar((paymentState as PaymentState.Error).message) }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                totalAmount = total,
                isLoading = isPlacingOrder || paymentState is PaymentState.Loading || paymentState is PaymentState.Verifying,
                onPlaceOrder = {
                    val currentUser = user ?: return@CheckoutBottomBar
                    checkoutViewModel.setPlacingOrder(true)
                    
                    val allMealIds = mutableListOf<String>()
                    cartState.meals.forEach { pair -> repeat(pair.second) { allMealIds.add(pair.first.id) } }
                    val planId = cartState.mealPlans.firstOrNull()?.first?.id

                    val finalOrder = Order(
                        customerId = currentUser.userId,
                        customerName = currentUser.displayName ?: "Guest",
                        vendorId = cartState.meals.firstOrNull()?.first?.vendorId ?: "",
                        businessName = "ZapFood Vendor",
                        items = cartState.meals.map { OrderItem(it.first.id, it.first.name, it.first.price, it.second) },
                        totalAmount = total,
                        deliveryFee = deliveryFee,
                        orderType = uiState.orderType,
                        deliveryDetails = if (uiState.orderType == OrderType.DELIVERY) uiState.deliveryDetails else null,
                        takeawayDetails = if (uiState.orderType == OrderType.TAKEAWAY) uiState.takeawayDetails else null,
                        dineInDetails = if (uiState.orderType == OrderType.DINE_IN) uiState.dineInDetails else null,
                        paymentMethod = uiState.paymentMethod
                    )

                    orderViewModel.placeOrder(currentUser, allMealIds, planId) { resource ->
                        checkoutViewModel.setPlacingOrder(false)
                        if (resource is Resource.Success) {
                            val order = resource.data!!
                            if (uiState.paymentMethod == PaymentMethod.CASH) {
                                cartViewModel.clearCart()
                                onOrderSuccess(order.orderId)
                            } else {
                                paymentViewModel.initiatePayment(order.orderId, currentUser.userId, total, uiState.paymentMethod)
                            }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(resource.message ?: "Failed to place order") }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { OrderTypeSelector(uiState.orderType, checkoutViewModel::setOrderType) }

            item {
                AnimatedContent(targetState = uiState.orderType, transitionSpec = { fadeIn() togetherWith fadeOut() }) { type ->
                    when (type) {
                        OrderType.DELIVERY -> DeliverySection(uiState.deliveryDetails) { checkoutViewModel.updateDeliveryDetails(it) }
                        OrderType.TAKEAWAY -> TakeawaySection(uiState.takeawayDetails) { checkoutViewModel.updateTakeawayDetails(it) }
                        OrderType.DINE_IN -> DineInSection(uiState.dineInDetails) { checkoutViewModel.updateDineInDetails(it) }
                    }
                }
            }

            item {
                if (pointsBalance > 0) {
                    RewardSection(pointsBalance, checkoutViewModel.pointsToRedeem) {
                        checkoutViewModel.pointsToRedeem = if (checkoutViewModel.pointsToRedeem > 0) 0 else pointsBalance
                    }
                }
            }

            item { PaymentMethodSection(uiState.paymentMethod, checkoutViewModel::setPaymentMethod) }

            item { OrderSummarySection(cartState, deliveryFee, rewardDiscount, total) }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun RewardSection(balance: Int, redeemed: Int, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rewards", fontWeight = FontWeight.Bold)
                Text("You have $balance points available", fontSize = 12.sp)
            }
            TextButton(onClick = onToggle) {
                Text(if (redeemed > 0) "Remove" else "Redeem All", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun OrderSummarySection(cartState: CartState, deliveryFee: Double, discount: Double, total: Double) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                cartState.meals.forEach { pair ->
                    SummaryRow(pair.first.name, "x${pair.second}", "ETB ${pair.first.price * pair.second}")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SummaryRow("Subtotal", "", "ETB ${cartState.subtotal}")
                if (deliveryFee > 0) SummaryRow("Delivery Fee", "", "ETB $deliveryFee")
                if (discount > 0) SummaryRow("Discount", "", "- ETB $discount", isNegative = true)
                Spacer(Modifier.height(8.dp))
                SummaryRow("Total", "", "ETB $total", isBold = true)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, qty: String, price: String, isBold: Boolean = false, isNegative: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Row {
            Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
            if (qty.isNotEmpty()) Text(" $qty", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Text(price, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium, color = if (isNegative) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DeliverySection(details: DeliveryDetails, onUpdate: (DeliveryDetails) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Delivery Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = details.address,
            onValueChange = { onUpdate(details.copy(address = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Delivery Address") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun TakeawaySection(details: TakeawayDetails, onUpdate: (TakeawayDetails) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pickup Branch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = details.pickupBranch,
            onValueChange = { onUpdate(details.copy(pickupBranch = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Select Branch") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun DineInSection(details: DineInDetails, onUpdate: (DineInDetails) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Table Reservation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = details.guestCount.toString(),
                onValueChange = { onUpdate(details.copy(guestCount = it.toIntOrNull() ?: 1)) },
                modifier = Modifier.weight(1f),
                label = { Text("Guests") },
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = "12:30 PM",
                onValueChange = { /* Placeholder */ },
                modifier = Modifier.weight(1f),
                label = { Text("Arrival Time") },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun PaymentMethodSection(selected: PaymentMethod, onSelect: (PaymentMethod) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaymentChip(PaymentMethod.CARD, "Card", selected == PaymentMethod.CARD) { onSelect(PaymentMethod.CARD) }
            PaymentChip(PaymentMethod.MOBILE_MONEY, "Mobile", selected == PaymentMethod.MOBILE_MONEY) { onSelect(PaymentMethod.MOBILE_MONEY) }
            PaymentChip(PaymentMethod.CASH, "Cash", selected == PaymentMethod.CASH) { onSelect(PaymentMethod.CASH) }
        }
    }
}

@Composable
fun PaymentChip(method: PaymentMethod, label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CheckoutBottomBar(totalAmount: Double, isLoading: Boolean, onPlaceOrder: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 16.dp) {
        Row(modifier = Modifier.padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Payable", style = MaterialTheme.typography.labelSmall)
                Text("ETB $totalAmount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = onPlaceOrder,
                enabled = !isLoading,
                modifier = Modifier.height(56.dp).width(160.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Place Order", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun openCheckoutInBrowser(context: Context, url: String) {
    try {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
