package com.example.food.domain.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.Feedback
import com.example.food.data.model.SupportTicket
import com.example.food.data.model.TicketResponse
import com.example.food.data.model.TicketStatus
import kotlinx.coroutines.flow.Flow

interface ISupportRepository {
    suspend fun saveTicket(ticket: SupportTicket): Resource<Unit>
    fun getUserTickets(userId: String): Flow<Resource<List<SupportTicket>>>
    fun getAllTickets(): Flow<Resource<List<SupportTicket>>>
    suspend fun updateTicketStatus(ticketId: String, newStatus: TicketStatus): Resource<Unit>
    
    suspend fun saveResponse(response: TicketResponse): Resource<Unit>
    fun getTicketResponses(ticketId: String): Flow<Resource<List<TicketResponse>>>
    
    suspend fun saveFeedback(feedback: Feedback): Resource<Unit>
    fun getAllFeedback(): Flow<Resource<List<Feedback>>>
}
