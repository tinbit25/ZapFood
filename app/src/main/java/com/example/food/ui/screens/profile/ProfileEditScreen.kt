package com.example.food.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.data.model.UserRole
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.PrimaryButton
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.ProfileState
import com.example.food.ui.viewmodel.ProfileViewModel
import com.example.food.ui.viewmodel.UserViewModel

@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var dietaryNeeds by remember { mutableStateOf(user?.dietaryNeeds?.joinToString(", ") ?: "") }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            onNavigateBack()
            profileViewModel.resetState()
        }
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
            Text(
                text = "General Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = displayName,
                onValueChange = { displayName = it },
                placeholder = "Display Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Specific Section
                Text(
                    text = "Dietary Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = dietaryNeeds,
                    onValueChange = { dietaryNeeds = it },
                    placeholder = "Dietary Needs (comma separated)",
                    leadingIcon = Icons.Default.Restaurant
                )

            Spacer(modifier = Modifier.height(48.dp))

            PrimaryButton(
                text = "Save Changes",
                onClick = {
                    val updatedUser = user?.copy(
                        displayName = displayName,
                        dietaryNeeds = dietaryNeeds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    )
                    if (updatedUser != null) {
                        profileViewModel.updateGeneralProfile(updatedUser)
                    }
                },
                enabled = profileState !is ProfileState.Loading,
                backgroundColor = Color(0xFFF16B24)
            )

            if (profileState is ProfileState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFFF16B24))
            }

            if (profileState is ProfileState.Error) {
                Text(
                    text = (profileState as ProfileState.Error).message,
                    color = Color(0xFFE57373),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
