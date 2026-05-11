package com.example.food.ui.screens.address

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.data.model.Address
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AddressViewModel
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun AddressFormScreen(
    addressId: String? = null,
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    addressViewModel: AddressViewModel
) {
    val user by userViewModel.user.collectAsState()
    val addresses by addressViewModel.addresses.collectAsState()
    val isLoading by addressViewModel.isLoading.collectAsState()

    val existingAddress = addresses.find { it.addressId == addressId }

    var label by remember { mutableStateOf(existingAddress?.label ?: "Home") }
    var city by remember { mutableStateOf(existingAddress?.city ?: "Addis Ababa") }
    var subcity by remember { mutableStateOf(existingAddress?.subcity ?: "") }
    var woreda by remember { mutableStateOf(existingAddress?.woreda ?: "") }
    var kebele by remember { mutableStateOf(existingAddress?.kebele ?: "") }
    var street by remember { mutableStateOf(existingAddress?.street ?: "") }
    var landmark by remember { mutableStateOf(existingAddress?.landmark ?: "") }
    var phoneNumber by remember { mutableStateOf(existingAddress?.phoneNumber ?: user?.phoneNumber ?: "") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: addresses.isEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(
            title = if (addressId == null) "Add Address" else "Edit Address",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = "Address Label (e.g. Home, Work)",
                leadingIcon = Icons.Default.Label
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            CustomTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = "City",
                leadingIcon = Icons.Default.LocationCity
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomTextField(
                        value = subcity,
                        onValueChange = { subcity = it },
                        placeholder = "Subcity",
                        leadingIcon = Icons.Default.Map
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    CustomTextField(
                        value = woreda,
                        onValueChange = { woreda = it },
                        placeholder = "Woreda",
                        leadingIcon = Icons.Default.Map
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomTextField(
                        value = kebele,
                        onValueChange = { kebele = it },
                        placeholder = "Kebele",
                        leadingIcon = Icons.Default.Map
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    CustomTextField(
                        value = street,
                        onValueChange = { street = it },
                        placeholder = "Street",
                        leadingIcon = Icons.Default.Traffic
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = landmark,
                onValueChange = { landmark = it },
                placeholder = "Landmark (e.g. Near Unity University)",
                leadingIcon = Icons.Default.Flag
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Contact Phone Number",
                leadingIcon = Icons.Default.Phone
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF16B24))
                )
                Text("Set as default delivery address", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = if (addressId == null) "Save Address" else "Update Address",
                onClick = {
                    user?.userId?.let { uid ->
                        val address = Address(
                            addressId = addressId ?: "",
                            label = label,
                            city = city,
                            subcity = subcity,
                            woreda = woreda,
                            kebele = kebele,
                            street = street,
                            landmark = landmark,
                            phoneNumber = phoneNumber,
                            isDefault = isDefault
                        )
                        addressViewModel.addAddress(uid, address)
                        onNavigateBack()
                    }
                },
                enabled = !isLoading && subcity.isNotEmpty() && woreda.isNotEmpty(),
                backgroundColor = Color(0xFFF16B24)
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFFF16B24))
            }
        }
    }
}
