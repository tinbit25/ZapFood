package com.example.food.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.food.data.model.UserRole
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedProfileEditScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    val uploadProgress by userViewModel.uploadProgress.collectAsState()

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phoneNumber by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var gender by remember { mutableStateOf(user?.gender ?: "") }
    
    // Vendor specific
    var cuisineType by remember { mutableStateOf(user?.cuisineType ?: "") }
    var businessAddress by remember { mutableStateOf(user?.businessAddress ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { userViewModel.uploadProfilePicture(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        TopNavBar(title = "Edit Profile", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { imagePickerLauncher.launch("image/*") }
            ) {
                AsyncImage(
                    model = user?.photoUrl ?: "https://via.placeholder.com/150",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFF16B24), CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF16B24))
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Image",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.White
                    )
                }

                if (uploadProgress != null) {
                    CircularProgressIndicator(
                        progress = uploadProgress!! / 100f,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF16B24),
                        strokeWidth = 4.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle("General Info")
            CustomTextField(
                value = displayName,
                onValueChange = { displayName = it },
                placeholder = "Full Name",
                leadingIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                placeholder = "Phone Number",
                leadingIcon = Icons.Default.Phone
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SectionTitle("Identity & Bio")
            // Gender Dropdown Placeholder or Simple TextField for now
            CustomTextField(
                value = gender,
                onValueChange = { gender = it },
                placeholder = "Gender (Male/Female/Other)",
                leadingIcon = Icons.Default.Face
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = "A short bio about yourself...",
                leadingIcon = Icons.Default.Info,
                modifier = Modifier.height(120.dp)
            )

            if (user?.role == UserRole.VENDOR) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Business Information")
                CustomTextField(
                    value = cuisineType,
                    onValueChange = { cuisineType = it },
                    placeholder = "Cuisine Type",
                    leadingIcon = Icons.Default.Restaurant
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField(
                    value = businessAddress,
                    onValueChange = { businessAddress = it },
                    placeholder = "Business Address",
                    leadingIcon = Icons.Default.Business
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = "Save Profile",
                onClick = {
                    user?.let { curr ->
                        val updated = curr.copy(
                            displayName = displayName,
                            email = email,
                            phoneNumber = phoneNumber,
                            gender = gender,
                            bio = bio,
                            cuisineType = cuisineType,
                            businessAddress = businessAddress
                        )
                        userViewModel.updateProfile(updated)
                        onNavigateBack()
                    }
                },
                backgroundColor = Color(0xFFF16B24)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}
