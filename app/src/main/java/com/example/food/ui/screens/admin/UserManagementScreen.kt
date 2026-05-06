package com.example.food.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.ui.components.CustomTextField
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.AdminViewModel

@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val usersState by viewModel.usersState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopNavBar(title = "User Management", onBackClick = onNavigateBack)

            Column(modifier = Modifier.padding(24.dp)) {
                CustomTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.fetchUsers(it, selectedRole)
                    },
                    placeholder = "Search by name or email...",
                    leadingIcon = Icons.Default.Search
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == null,
                        onClick = { 
                            selectedRole = null
                            viewModel.fetchUsers(searchQuery, null)
                        },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.CUSTOMER,
                        onClick = { 
                            selectedRole = UserRole.CUSTOMER
                            viewModel.fetchUsers(searchQuery, UserRole.CUSTOMER)
                        },
                        label = { Text("Customers") }
                    )
                    FilterChip(
                        selected = selectedRole == UserRole.VENDOR,
                        onClick = { 
                            selectedRole = UserRole.VENDOR
                            viewModel.fetchUsers(searchQuery, UserRole.VENDOR)
                        },
                        label = { Text("Vendors") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (usersState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFF16B24))
                        }
                    }
                    is Resource.Error -> {
                        Text(text = usersState.message ?: "Error loading users", color = Color.Red)
                    }
                    is Resource.Success -> {
                        val users = usersState.data ?: emptyList()
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(users) { user ->
                                UserCard(user, onStatusToggle = { viewModel.toggleUserStatus(user) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(user: User, onStatusToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1A1A)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl ?: "https://ui-avatars.com/api/?name=${user.displayName}",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.displayName ?: "Unnamed User", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = user.email, fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF16B24).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = user.role.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color(0xFFF16B24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!user.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "DEACTIVATED", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Switch(
                checked = user.isActive,
                onCheckedChange = { onStatusToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }
    }
}
