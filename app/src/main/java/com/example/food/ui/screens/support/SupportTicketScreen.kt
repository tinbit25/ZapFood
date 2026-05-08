package com.example.food.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.SupportTicket
import com.example.food.data.model.TicketCategory
import com.example.food.ui.viewmodel.SupportViewModel
import com.example.food.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    supportViewModel: SupportViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    
    val ticketsState by supportViewModel.ticketsState.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(TicketCategory.GENERAL) }
    var message by remember { mutableStateOf("") }
    var createStatus by remember { mutableStateOf<Resource<Unit>?>(null) }

    LaunchedEffect(user) {
        user?.let {
            supportViewModel.fetchMyTickets(it.userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Tickets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Text("+", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = ticketsState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is Resource.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                is Resource.Success -> {
                    val tickets = state.data ?: emptyList()
                    if (tickets.isEmpty()) {
                        Text("No support tickets found.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(tickets) { ticket ->
                                TicketItem(ticket)
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Support Ticket") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Category")
                        // Simple dropdown or radio group. Using a simple text for now
                        // In a real app, use DropdownMenu
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TicketCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Describe your issue") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        
                        if (createStatus is Resource.Loading) {
                            CircularProgressIndicator()
                        }
                        if (createStatus is Resource.Error) {
                            Text((createStatus as Resource.Error).message ?: "Error", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        user?.let { u ->
                            if (message.isNotBlank()) {
                                createStatus = Resource.Loading()
                                supportViewModel.createTicket(u, selectedCategory, message, null) { result ->
                                    createStatus = result
                                    if (result is Resource.Success) {
                                        showCreateDialog = false
                                        message = ""
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TicketItem(ticket: SupportTicket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Ticket: ${ticket.category.name}", style = MaterialTheme.typography.titleMedium)
                Badge(
                    containerColor = when(ticket.status) {
                        com.example.food.data.model.TicketStatus.OPEN -> MaterialTheme.colorScheme.primary
                        com.example.food.data.model.TicketStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
                        com.example.food.data.model.TicketStatus.RESOLVED -> MaterialTheme.colorScheme.tertiary
                        com.example.food.data.model.TicketStatus.CLOSED -> MaterialTheme.colorScheme.outline
                    }
                ) {
                    Text(ticket.status.name, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = ticket.message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(ticket.createdAt))
            Text(text = "Submitted: $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
