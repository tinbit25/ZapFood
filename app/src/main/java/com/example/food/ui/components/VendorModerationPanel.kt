package com.example.food.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.domain.usecase.VendorAction

@Composable
fun VendorModerationPanel(
    onActionClick: (VendorAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Moderation Control Panel",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModerationButton(
                    label = "Approve",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    onClick = { onActionClick(VendorAction.APPROVE) }
                )
                ModerationButton(
                    label = "Reject",
                    icon = Icons.Default.Cancel,
                    color = Color(0xFFF44336),
                    onClick = { onActionClick(VendorAction.REJECT) }
                )
                ModerationButton(
                    label = "Suspend",
                    icon = Icons.Default.Block,
                    color = Color(0xFFFF9800),
                    onClick = { onActionClick(VendorAction.SUSPEND) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModerationButton(
                    label = "Request Info",
                    icon = Icons.Default.Info,
                    color = Color(0xFF2196F3),
                    onClick = { onActionClick(VendorAction.REQUEST_INFO) }
                )
                ModerationButton(
                    label = "Flag",
                    icon = Icons.Default.Flag,
                    color = Color(0xFFE91E63),
                    onClick = { onActionClick(VendorAction.FLAG) }
                )
            }
        }
    }
}

@Composable
fun ModerationButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f), contentColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(100.dp)
            .height(72.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
