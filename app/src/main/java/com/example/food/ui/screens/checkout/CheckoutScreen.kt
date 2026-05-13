package com.example.food.ui.screens.checkout

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.data.model.*
import com.example.food.ui.components.OrderTypeSelector
import com.example.food.ui.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onNavigateBack: () -> Unit,
    onPlaceOrder: (Order) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
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
                totalAmount = 1500.0, // Should be passed from cart
                onPlaceOrder = { /* Logic to create Order object */ }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Order Type Selection
            item {
                OrderTypeSelector(
                    selectedType = uiState.orderType,
                    onTypeSelected = viewModel::setOrderType
                )
            }

            // 2. Dynamic Details Based on Type
            item {
                AnimatedContent(
                    targetState = uiState.orderType,
                    transitionSpec = { fadeIn() with fadeOut() }
                ) { type ->
                    when (type) {
                        OrderType.DELIVERY -> DeliverySection(uiState.deliveryDetails)
                        OrderType.TAKEAWAY -> TakeawaySection(uiState.takeawayDetails)
                        OrderType.DINE_IN -> DineInSection(uiState.dineInDetails)
                    }
                }
            }

            // 3. Payment Method
            item {
                PaymentMethodSection(
                    selectedMethod = uiState.paymentMethod,
                    onMethodSelected = viewModel::setPaymentMethod
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DeliverySection(details: DeliveryDetails) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Delivery Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = details.address,
            onValueChange = { /* Update */ },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your address in Addis Ababa") },
            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.Red) },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = details.instructions ?: "",
            onValueChange = { /* Update */ },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Delivery instructions (e.g., Gate code)") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun TakeawaySection(details: TakeawayDetails) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pickup Branch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Bole Branch", fontWeight = FontWeight.Bold)
                    Text("Ready for pickup in ~25 mins", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DineInSection(details: DineInDetails) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dine-In Reservation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = details.guestCount.toString(),
                onValueChange = { /* Update */ },
                modifier = Modifier.weight(1f),
                label = { Text("Guests") },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = "12:30 PM",
                onValueChange = { /* Update */ },
                modifier = Modifier.weight(1f),
                label = { Text("Arrival Time") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Traditional fasting meals will be prepared before your arrival.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PaymentMethodSection(selectedMethod: PaymentMethod, onMethodSelected: (PaymentMethod) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Payment Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        PaymentOption(
            method = PaymentMethod.CARD,
            title = "Credit/Debit Card",
            subtitle = "Pay with Chapa",
            icon = Icons.Default.CreditCard,
            isSelected = selectedMethod == PaymentMethod.CARD,
            onClick = { onMethodSelected(PaymentMethod.CARD) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        PaymentOption(
            method = PaymentMethod.MOBILE_MONEY,
            title = "Telebirr / M-Pesa",
            subtitle = "Direct mobile payment",
            icon = Icons.Default.PhoneAndroid,
            isSelected = selectedMethod == PaymentMethod.MOBILE_MONEY,
            onClick = { onMethodSelected(PaymentMethod.MOBILE_MONEY) }
        )
    }
}

@Composable
fun PaymentOption(
    method: PaymentMethod,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
fun CheckoutBottomBar(totalAmount: Double, onPlaceOrder: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Payable", style = MaterialTheme.typography.labelMedium)
                Text("ETB ${totalAmount.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            
            Button(
                onClick = onPlaceOrder,
                modifier = Modifier
                    .height(56.dp)
                    .width(180.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
