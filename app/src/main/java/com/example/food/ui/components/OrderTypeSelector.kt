package com.example.food.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.data.model.OrderType

@Composable
fun OrderTypeSelector(
    selectedType: OrderType,
    onTypeSelected: (OrderType) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Select Service Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OrderTypeCard(
                type = OrderType.DELIVERY,
                title = "Delivery",
                subtitle = "To your door",
                icon = Icons.Default.LocalShipping,
                isSelected = selectedType == OrderType.DELIVERY,
                onClick = { if (enabled) onTypeSelected(OrderType.DELIVERY) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
            
            OrderTypeCard(
                type = OrderType.TAKEAWAY,
                title = "Takeaway",
                subtitle = "Pick up food",
                icon = Icons.Default.ShoppingBag,
                isSelected = selectedType == OrderType.TAKEAWAY,
                onClick = { if (enabled) onTypeSelected(OrderType.TAKEAWAY) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
            
            OrderTypeCard(
                type = OrderType.DINE_IN,
                title = "Dine-In",
                subtitle = "Eat at venue",
                icon = Icons.Default.Restaurant,
                isSelected = selectedType == OrderType.DINE_IN,
                onClick = { if (enabled) onTypeSelected(OrderType.DINE_IN) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }
    }
}

@Composable
private fun OrderTypeCard(
    type: OrderType,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    OutlinedCard(
        modifier = modifier
            .height(110.dp)
            .alpha(if (enabled || isSelected) 1f else 0.5f)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = contentColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = contentColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
