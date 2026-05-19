package com.example.food.ui.screens.admin

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
import com.example.food.data.model.TicketStatus
import com.example.food.ui.viewmodel.SupportViewModel
import com.example.food.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

import com.example.food.ui.viewmodel.FeedbackViewModel
import com.example.food.data.model.Feedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportDashboardScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    supportViewModel: SupportViewModel = viewModel(),
    feedbackViewModel: FeedbackViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    val ticketsState by supportViewModel.ticketsState.collectAsState()
    val platformFeedbackState by feedbackViewModel.feedbackListState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tickets", "Platform Feedback")

    LaunchedEffect(user) {
        user?.let {
            supportViewModel.fetchAllTickets(it)
            feedbackViewModel.fetchAllFeedback(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TicketsTab(ticketsState, supportViewModel, user)
                1 -> FeedbackTab(platformFeedbackState)
            }
        }
    }
}

@Composable
fun TicketsTab(
    ticketsState: Resource<List<SupportTicket>>,
    supportViewModel: SupportViewModel,
    user: com.example.food.data.model.User?
) {
    var selectedStatusFilter by remember { mutableStateOf<TicketStatus?>(null) }
    var selectedTicket by remember { mutableStateOf<SupportTicket?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = TicketStatus.values().indexOf(selectedStatusFilter) + 1,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedStatusFilter == null,
                onClick = { selectedStatusFilter = null },
                text = { Text("ALL") }
            )
            TicketStatus.values().forEach { status ->
                Tab(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = status },
                    text = { Text(status.name) }
                )
            }
        }

        when (val state = ticketsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            is Resource.Success -> {
                val tickets = state.data ?: emptyList()
                val filteredTickets = if (selectedStatusFilter == null) tickets else tickets.filter { it.status == selectedStatusFilter }
                
                if (filteredTickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No tickets found.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredTickets) { ticket ->
                            AdminTicketItem(
                                ticket = ticket,
                                onClick = { selectedTicket = ticket },
                                onUpdateStatus = { newStatus ->
                                    user?.let { u ->
                                        supportViewModel.updateTicketStatus(u, ticket.ticketId, newStatus) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedTicket != null) {
        val currentTicket = (ticketsState as? Resource.Success)?.data?.find { it.ticketId == selectedTicket!!.ticketId } ?: selectedTicket!!
        AdminTicketDetailsDialog(
            ticket = currentTicket,
            user = user,
            supportViewModel = supportViewModel,
            onDismiss = { selectedTicket = null },
            onUpdateStatus = { newStatus ->
                user?.let { u ->
                    supportViewModel.updateTicketStatus(u, currentTicket.ticketId, newStatus) {}
                }
            }
        )
    }
}

@Composable
fun AdminTicketDetailsDialog(
    ticket: SupportTicket,
    user: com.example.food.data.model.User?,
    supportViewModel: SupportViewModel,
    onDismiss: () -> Unit,
    onUpdateStatus: (TicketStatus) -> Unit
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
                Text(text = "Ticket Details (Admin)", style = MaterialTheme.typography.titleMedium)
                Text(text = "Category: ${ticket.category.name} | User: ${ticket.userName}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Status:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    TicketStatus.values().forEach { status ->
                        FilterChip(
                            selected = ticket.status == status,
                            onClick = { onUpdateStatus(status) },
                            label = { Text(status.name) }
                        )
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
                        placeholder = { Text("Type an answer...") },
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
fun FeedbackTab(feedbackState: Resource<List<Feedback>>) {
    when (val state = feedbackState) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is Resource.Error -> {
            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }
        is Resource.Success -> {
            val feedbackList = state.data ?: emptyList()
            if (feedbackList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No platform feedback yet.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(feedbackList) { feedback ->
                        AdminFeedbackItem(feedback)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFeedbackItem(feedback: Feedback) {
    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(feedback.createdAt))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "User: ${feedback.userName}", style = MaterialTheme.typography.titleSmall)
                Text(text = "Rating: ${feedback.rating}/5", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (!feedback.vendorName.isNullOrEmpty()) {
                Text(
                    text = "Vendor: ${feedback.vendorName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = feedback.comment, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Date: $date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!feedback.orderId.isNullOrEmpty()) {
                Text(
                    text = "Order ID: ${feedback.orderId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminTicketItem(
    ticket: SupportTicket,
    onClick: () -> Unit,
    onUpdateStatus: (TicketStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "User: ${ticket.userName}", style = MaterialTheme.typography.titleMedium)
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
            Text(text = "Submitted: $date", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TicketStatus.values().forEach { status ->
                    FilterChip(
                        selected = ticket.status == status,
                        onClick = { onUpdateStatus(status) },
                        label = { Text(status.name) }
                    )
                }
            }
        }
    }
}
