package com.example.food.ui.screens.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food.core.util.Resource
import com.example.food.data.model.SupportTicket
import com.example.food.data.model.TicketCategory
import com.example.food.data.model.TicketResponse
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
    var selectedTicket by remember { mutableStateOf<SupportTicket?>(null) }

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
                                TicketItem(ticket, onClick = { selectedTicket = ticket })
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

        if (selectedTicket != null) {
            TicketDetailsDialog(
                ticket = selectedTicket!!,
                user = user,
                supportViewModel = supportViewModel,
                onDismiss = { selectedTicket = null }
            )
        }
    }
}

@Composable
fun TicketDetailsDialog(
    ticket: SupportTicket,
    user: com.example.food.data.model.User?,
    supportViewModel: SupportViewModel,
    onDismiss: () -> Unit
) {
    val responsesState by supportViewModel.responsesState.collectAsState()
    var replyText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(ticket.ticketId) {
        supportViewModel.fetchResponses(ticket.ticketId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Ticket Details", style = MaterialTheme.typography.titleMedium)
                Text(text = "Category: ${ticket.category.name}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = ticket.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = ticket.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    when (val state = responsesState) {
                        is Resource.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is Resource.Error -> {
                            Text("Failed to load responses: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                        is Resource.Success -> {
                            val responses = state.data ?: emptyList()
                            if (responses.isEmpty()) {
                                Text("No responses yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(responses) { resp ->
                                        val isMe = resp.senderId == user?.userId
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                        ) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                                ),
                                                modifier = Modifier.widthIn(max = 240.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(text = resp.senderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(text = resp.message, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Type a reply...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Button(
                        onClick = {
                            val u = user ?: return@Button
                            if (replyText.isNotBlank()) {
                                isSending = true
                                supportViewModel.sendResponse(u, ticket.ticketId, replyText) { res ->
                                    isSending = false
                                    if (res is Resource.Success) {
                                        replyText = ""
                                        supportViewModel.fetchResponses(ticket.ticketId)
                                    }
                                }
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSending
                    ) {
                        Text("Send")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TicketItem(ticket: SupportTicket, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
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
