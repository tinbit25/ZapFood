package com.example.food.ui.screens.admin

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
import com.example.food.data.model.TicketStatus
import com.example.food.ui.viewmodel.SupportViewModel
import com.example.food.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportDashboardScreen(
    onNavigateBack: () -> Unit,
    userViewModel: UserViewModel,
    supportViewModel: SupportViewModel = viewModel()
) {
    val user by userViewModel.user.collectAsState()
    
    val ticketsState by supportViewModel.ticketsState.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<TicketStatus?>(null) }

    LaunchedEffect(user) {
        user?.let {
            supportViewModel.fetchAllTickets(it)
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
            
            // Filters
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
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
                is Resource.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                is Resource.Success -> {
                    val tickets = state.data ?: emptyList()
                    val filteredTickets = if (selectedStatusFilter == null) tickets else tickets.filter { it.status == selectedStatusFilter }
                    
                    if (filteredTickets.isEmpty()) {
                        Text("No tickets found.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredTickets) { ticket ->
                                AdminTicketItem(
                                    ticket = ticket,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTicketItem(
    ticket: SupportTicket,
    onUpdateStatus: (TicketStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "User: ${ticket.userName}", style = MaterialTheme.typography.titleMedium)
                Text(text = ticket.category.name, style = MaterialTheme.typography.labelMedium)
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
