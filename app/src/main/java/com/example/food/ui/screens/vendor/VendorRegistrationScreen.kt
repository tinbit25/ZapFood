package com.example.food.ui.screens.vendor

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.data.model.VendorType
import com.example.food.data.model.ServiceTag
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.VendorRegistrationState
import com.example.food.ui.viewmodel.VendorRegistrationViewModel

@Composable
fun VendorRegistrationScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: VendorRegistrationViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    val uiState by viewModel.uiState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState) {
        if (uiState is VendorRegistrationState.Success) {
            onRegistrationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        TopNavBar(title = "Vendor Registration", onBackClick = onNavigateBack)

        // Step Indicator
        RegistrationStepper(currentStep = currentStep)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> BasicInfoStep(viewModel)
                    2 -> OperationsStep(viewModel)
                    3 -> VerificationStep(viewModel)
                }
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back")
                }
            }

            PrimaryButton(
                text = if (currentStep == 3) "Submit Application" else "Continue",
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        viewModel.register(userId)
                    }
                },
                modifier = Modifier.weight(1f),
                isLoading = uiState is VendorRegistrationState.Loading
            )
        }
    }

    if (uiState is VendorRegistrationState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Registration Error") },
            text = { Text((uiState as VendorRegistrationState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun RegistrationStepper(currentStep: Int) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepCircle(step = 1, currentStep = currentStep, label = "Business")
        StepperLine(active = currentStep >= 2)
        StepCircle(step = 2, currentStep = currentStep, label = "Operations")
        StepperLine(active = currentStep >= 3)
        StepCircle(step = 3, currentStep = currentStep, label = "Legal")
    }
}

@Composable
fun StepCircle(step: Int, currentStep: Int, label: String) {
    val active = step <= currentStep
    val completed = step < currentStep
    val colorScheme = MaterialTheme.colorScheme

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (active) colorScheme.primary else colorScheme.surfaceVariant)
                .border(2.dp, if (active) colorScheme.primary else colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (completed) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = step.toString(),
                    color = if (active) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = if (active) colorScheme.primary else colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RowScope.StepperLine(active: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .padding(horizontal = 4.dp)
            .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    )
}

@Composable
fun BasicInfoStep(viewModel: VendorRegistrationViewModel) {
    val businessName by viewModel.businessName.collectAsState()
    val description by viewModel.description.collectAsState()
    val businessTypes by viewModel.businessTypes.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Tell us about your business", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = businessName,
            onValueChange = { viewModel.businessName.value = it },
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text("Business Categories (Select all that apply)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VendorType.entries.forEach { type ->
                val selected = businessTypes.contains(type)
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.toggleBusinessType(type) },
                    label = { Text(type.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.description.value = it },
            label = { Text("Business Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
    }
}

@Composable
fun OperationsStep(viewModel: VendorRegistrationViewModel) {
    val phone by viewModel.phoneNumber.collectAsState()
    val radius by viewModel.deliveryRadius.collectAsState()
    val selectedTags by viewModel.serviceTags.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Operational Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = phone,
            onValueChange = { viewModel.phoneNumber.value = it },
            label = { Text("Business Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Text("+251 ", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold) }
        )

        Text("Services Offered", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceTag.entries.forEach { tag ->
                val selected = selectedTags.contains(tag)
                Surface(
                    onClick = { viewModel.toggleServiceTag(tag) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tag.icon, fontSize = 20.sp)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = tag.displayName,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Checkbox(
                            checked = selected,
                            onCheckedChange = { viewModel.toggleServiceTag(tag) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        Column {
            Text("Delivery Radius: ${radius.toInt()} km", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = radius.toFloat(),
                onValueChange = { viewModel.deliveryRadius.value = it.toDouble() },
                valueRange = 1f..50f,
                steps = 49,
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun VerificationStep(viewModel: VendorRegistrationViewModel) {
    val taxId by viewModel.taxId.collectAsState()
    val bankInfo by viewModel.bankInfo.collectAsState()
    val mobileMoney by viewModel.mobileMoney.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Legal & Financial Info", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "This information is only visible to our verification team.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = taxId,
            onValueChange = { viewModel.taxId.value = it },
            label = { Text("Tax Identification Number (TIN)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = bankInfo,
            onValueChange = { viewModel.bankInfo.value = it },
            label = { Text("Bank Account Info (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Bank Name, Account Number") }
        )

        OutlinedTextField(
            value = mobileMoney,
            onValueChange = { viewModel.mobileMoney.value = it },
            label = { Text("Mobile Money Number (CBE Birr / Telebirr)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Text("+251 ", modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold) }
        )

        // Document Upload Placeholders
        DocumentUploadItem(title = "Business License", icon = Icons.Default.Description)
        DocumentUploadItem(title = "Sanitation Certificate", icon = Icons.Default.HealthAndSafety)
    }
}

@Composable
fun DocumentUploadItem(title: String, icon: ImageVector) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle Upload */ },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
