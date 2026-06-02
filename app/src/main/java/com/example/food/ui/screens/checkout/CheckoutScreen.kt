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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CheckoutScreen(
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    rewardViewModel: RewardViewModel,
    paymentViewModel: PaymentViewModel,
    checkoutViewModel: CheckoutViewModel,
    smartTableViewModel: SmartTableViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val user by userViewModel.user.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val uiState by checkoutViewModel.uiState.collectAsState()
    val smartTableState by smartTableViewModel.uiState.collectAsState()
    val isPlacingOrder by checkoutViewModel.isPlacingOrder.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val pointsBalance by rewardViewModel.pointsBalance.collectAsState()
    val locationState by locationViewModel.locationState.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            locationViewModel.getCurrentLocation(context)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Location permission is required to get your current location")
            }
        }
    }

    LaunchedEffect(Unit) {
        locationViewModel.initialize(context)
    }

    LaunchedEffect(locationState.address) {
        if (locationState.address.isNotEmpty()) {
            checkoutViewModel.updateDeliveryInfo(
                uiState.deliveryInfo?.copy(
                    address = locationState.address,
                    city = locationState.city,
                    latitude = locationState.latitude,
                    longitude = locationState.longitude
                ) ?: DeliveryDetails(
                    address = locationState.address,
                    city = locationState.city,
                    latitude = locationState.latitude,
                    longitude = locationState.longitude
                )
            )
        }
    }

    LaunchedEffect(locationState.error) {
        locationState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    val pricingSummary = remember(cartState.subtotal, uiState.orderType, checkoutViewModel.pointsToRedeem) {
        com.example.food.domain.manager.CheckoutPricingManager().getPricingSummary(
            subtotal = cartState.subtotal,
            orderType = uiState.orderType,
            pointsToRedeem = checkoutViewModel.pointsToRedeem
        )
    }
    val rewardDiscount = pricingSummary.discount
    val deliveryFee = pricingSummary.deliveryFee
    val packagingFee = pricingSummary.packagingFee
    val subtotal = pricingSummary.subtotal
    val total = pricingSummary.total

    LaunchedEffect(user) {
        user?.let { rewardViewModel.fetchBalance(it.userId) }
    }

    LaunchedEffect(smartTableState.session) {
        smartTableState.session?.let { session ->
            checkoutViewModel.initializeWithSession(session)
        }
    }

    // Payment Verification Logic
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (paymentState is PaymentState.CheckoutReady && com.example.food.MainActivity.pendingPaymentReturn) {
                    com.example.food.MainActivity.clearPaymentReturn()
                    paymentViewModel.verifyPayment()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                isDineIn = uiState.orderType == OrderType.DINE_IN,
                onPlaceOrder = {
                    val currentUser = user ?: return@CheckoutBottomBar
                    checkoutViewModel.setPlacingOrder(true)
                    
                    val allMealIds = mutableListOf<String>()
                    cartState.meals.forEach { pair -> repeat(pair.second) { allMealIds.add(pair.first.id) } }
                    val planId = cartState.mealPlans.firstOrNull()?.first?.id

                    // Build the base order; CheckoutViewModel enriches type-specific info internally
                    val baseOrder = Order(
                        customerId = currentUser.userId,
                        customerName = currentUser.displayName ?: "Guest",
                        customerPhone = uiState.deliveryInfo?.contactPhone ?: currentUser.phoneNumber ?: "",
                        vendorId = cartState.meals.firstOrNull()?.first?.vendorId ?: "",
                        businessName = "ZapFood Vendor",
                        items = cartState.meals.map { OrderItem(it.first.id, it.first.name, it.first.price, it.second) },
                        totalAmount = total,
                        deliveryFee = deliveryFee,
                        orderType = uiState.orderType,
                        // Unified info fields
                        deliveryInfo = if (uiState.orderType == OrderType.DELIVERY) uiState.deliveryInfo else null,
                        pickupInfo   = if (uiState.orderType == OrderType.TAKEAWAY) uiState.pickupInfo   else null,
                        dineInInfo   = if (uiState.orderType == OrderType.DINE_IN)  uiState.dineInInfo   else null,
                        paymentMethod = uiState.paymentMethod
                    )

                    // Route through UnifiedOrderManager via CheckoutViewModel
                    checkoutViewModel.placeOrder(baseOrder) { resource ->
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
            item { 
                val isSessionActive = smartTableState.session != null
                OrderTypeSelector(
                    selectedType = uiState.orderType, 
                    onTypeSelected = checkoutViewModel::setOrderType,
                    enabled = !isSessionActive
                )
                if (isSessionActive) {
                    Text(
                        text = "You are currently at Table ${smartTableState.session?.tableNumber}. Order will be served there.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                AnimatedContent(targetState = uiState.orderType, transitionSpec = { fadeIn() togetherWith fadeOut() }) { type ->
                    when (type) {
                        OrderType.DELIVERY -> DeliverySection(uiState.deliveryInfo, locationViewModel, context, locationPermissionLauncher) { checkoutViewModel.updateDeliveryInfo(it) }
                        OrderType.TAKEAWAY -> TakeawaySection(uiState.pickupInfo)   { checkoutViewModel.updatePickupInfo(it) }
                        OrderType.DINE_IN  -> DineInSection(uiState.dineInInfo)    { checkoutViewModel.updateDineInInfo(it) }
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

            item { OrderSummarySection(cartState, deliveryFee, packagingFee, rewardDiscount, total) }
            
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
fun OrderSummarySection(cartState: CartState, deliveryFee: Double, packagingFee: Double, discount: Double, total: Double) {
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
                if (packagingFee > 0) SummaryRow("Packaging Fee", "", "ETB $packagingFee")
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
fun DeliverySection(
    details: DeliveryDetails,
    locationViewModel: LocationViewModel,
    context: Context,
    locationPermissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onUpdate: (DeliveryDetails) -> Unit
) {
    val locationState by locationViewModel.locationState.collectAsState()

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
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = details.instructions ?: "",
            onValueChange = { onUpdate(details.copy(instructions = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Delivery Instructions") },
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = details.contactPhone ?: "",
            onValueChange = { onUpdate(details.copy(contactPhone = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contact Phone Number") },
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Get Current Location Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Current Location",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (details.latitude != null && details.longitude != null) {
                            Text(
                                text = "Lat: ${String.format("%.6f", details.latitude)}, Lng: ${String.format("%.6f", details.longitude)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "Tap to get your current location",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                locationViewModel.getCurrentLocation(context)
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(40.dp),
                        enabled = !locationState.isLoading
                    ) {
                        if (locationState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Location", fontSize = 12.sp)
                        }
                    }
                }
                if (locationState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = locationState.error!!,
                        color = Color.Red,
                        fontSize = 11.sp
                    )
                }
            }
        }
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
                value = details.expectedArrivalTime,
                onValueChange = { onUpdate(details.copy(expectedArrivalTime = it)) },
                modifier = Modifier.weight(1f),
                label = { Text("Arrival Time") },
                placeholder = { Text("12:30 PM") },
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
fun CheckoutBottomBar(totalAmount: Double, isLoading: Boolean, isDineIn: Boolean = false, onPlaceOrder: () -> Unit) {
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
                else Text(if (isDineIn) "Book Table" else "Place Order", fontWeight = FontWeight.Bold)
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
