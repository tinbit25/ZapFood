package com.example.food.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    paymentViewModel: PaymentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val cartState by cartViewModel.cartState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val deliveryFee = 2000.0 // RWF
    val total = (cartState.subtotal * 1000) + deliveryFee

    val paymentState by paymentViewModel.paymentState.collectAsState()
    
    var selectedAddress by remember { mutableStateOf("123 Main St, Kigali, Rwanda") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var isPlacingOrder by remember { mutableStateOf(false) }

    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.Success) {
            cartViewModel.clearCart()
            paymentViewModel.resetState()
            onOrderSuccess()
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
                        SummaryRow(plan.name, "x$qty", "RWF ${"%,.0f".format(plan.price * qty * 1000)}")
                    }
                    for (pair in cartState.meals) {
                        val meal = pair.first
                        val qty = pair.second
                        SummaryRow(meal.name, "x$qty", "RWF ${"%,.0f".format(meal.price * qty * 1000)}")
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.3f))
                    
                    SummaryRow("Subtotal", "", "RWF ${"%,.0f".format(cartState.subtotal * 1000)}")
                    SummaryRow("Delivery Fee", "", "RWF ${"%,.0f".format(deliveryFee)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    SummaryRow("Total", "", "RWF ${"%,.0f".format(total)}", isBold = true)
                }
            }

            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = when {
                        isPlacingOrder -> "Placing Order..."
                        paymentState is PaymentState.Loading -> "Processing Payment..."
                        else -> "Confirm Order"
                    },
                    enabled = !isPlacingOrder && paymentState !is PaymentState.Loading && (cartState.meals.isNotEmpty() || cartState.mealPlans.isNotEmpty()),
                    onClick = {
                        val currentUser = user
                        if (currentUser == null) {
                            scope.launch { snackbarHostState.showSnackbar("User not authenticated") }
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
                                        // Cash orders don't need digital initiation
                                        cartViewModel.clearCart()
                                        onOrderSuccess()
                                    } else {
                                        // Initiate digital payment
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

                if (paymentState is PaymentState.Error) {
                    Text(
                        text = (paymentState as PaymentState.Error).message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.Center)
                    )
                }
            }
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
