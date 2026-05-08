package com.example.food.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.usecase.SupportUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class SupportViewModel(
    private val supportUseCase: SupportUseCase = SupportUseCase()
) : ViewModel() {

    private val _ticketsState = MutableStateFlow<Resource<List<SupportTicket>>>(Resource.Loading())
    val ticketsState: StateFlow<Resource<List<SupportTicket>>> = _ticketsState.asStateFlow()

    private val _responsesState = MutableStateFlow<Resource<List<TicketResponse>>>(Resource.Loading())
    val responsesState: StateFlow<Resource<List<TicketResponse>>> = _responsesState.asStateFlow()

    fun createTicket(
        user: User,
        category: TicketCategory,
        message: String,
        orderId: String? = null,
        onResult: (Resource<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = supportUseCase.createTicket(user, category, message, orderId)
            onResult(result)
            if (result is Resource.Success) {
                fetchMyTickets(user.userId)
            }
        }
    }

    fun fetchMyTickets(userId: String) {
        viewModelScope.launch {
            supportUseCase.getMyTickets(userId).collect {
                _ticketsState.value = it
            }
        }
    }

    fun fetchAllTickets(user: User) {
        viewModelScope.launch {
            supportUseCase.getAllTickets(user).collect {
                _ticketsState.value = it
            }
        }
    }

    fun updateTicketStatus(user: User, ticketId: String, newStatus: TicketStatus, onResult: (Resource<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = supportUseCase.updateStatus(user, ticketId, newStatus)
            onResult(result)
            if (result is Resource.Success) {
                fetchAllTickets(user)
            }
        }
    }

    fun fetchResponses(ticketId: String) {
        viewModelScope.launch {
            supportUseCase.getResponses(ticketId).collect {
                _responsesState.value = it
            }
        }
    }

    fun sendResponse(
        user: User,
        ticketId: String,
        message: String,
        onResult: (Resource<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = supportUseCase.respondToTicket(user, ticketId, message)
            onResult(result)
        }
    }
}
