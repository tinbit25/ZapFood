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
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.CartViewModel

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val cartState by cartViewModel.cartState.collectAsState()
    val deliveryFee = 2000.0 // RWF
    val total = (cartState.subtotal * 1000) + deliveryFee

    var selectedAddress by remember { mutableStateOf("123 Main St, Kigali, Rwanda") }
    var selectedPayment by remember { mutableStateOf("Momo Pay (**** 1234)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
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
            CheckoutOptionCard(
                title = selectedPayment,
                icon = Icons.Default.Payment,
                onClick = { /* Navigate to payment selection */ }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Order Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                cartState.mealPlans.forEach { (plan, qty) ->
                    SummaryRow(plan.name, "x$qty", "RWF ${"%,.0f".format(plan.price * qty * 1000)}")
                }
                cartState.meals.forEach { (meal, qty) ->
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
                text = "Confirm Order",
                onClick = {
                    cartViewModel.clearCart()
                    onOrderSuccess()
                },
                backgroundColor = Color(0xFFF16B24)
            )
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
