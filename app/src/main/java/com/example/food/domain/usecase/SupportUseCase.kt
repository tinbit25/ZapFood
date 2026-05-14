package com.example.food.domain.usecase

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.repository.ISupportRepository
import com.example.food.data.repository.SupportRepository
import kotlinx.coroutines.flow.Flow

class SupportUseCase(
    private val supportRepository: ISupportRepository = SupportRepository()
) {
    suspend fun createTicket(
        user: User,
        category: TicketCategory,
        message: String,
        orderId: String? = null
    ): Resource<Unit> {
        if (message.isBlank()) return Resource.Error("Message cannot be empty")
        
        val ticket = SupportTicket(
            userId = user.userId,
            userName = user.displayName ?: "Unknown User",
            relatedOrderId = orderId,
            category = category,
            message = message,
            status = TicketStatus.OPEN
        )
        
        val result = supportRepository.saveTicket(ticket)
        if (result is Resource.Success) {
            // Mock notification hook
            println("NOTIFICATION: Ticket created by ${user.displayName}. Admins notified.")
        }
        return result
    }

    fun getMyTickets(userId: String): Flow<Resource<List<SupportTicket>>> {
        return supportRepository.getUserTickets(userId)
    }

    fun getAllTickets(user: User): Flow<Resource<List<SupportTicket>>> {
        return supportRepository.getAllTickets()
    }

    suspend fun updateStatus(
        user: User,
        ticketId: String,
        newStatus: TicketStatus
    ): Resource<Unit> {
        if (user.role != UserRole.ADMIN) {
            return Resource.Error("Unauthorized: Only admins can update ticket statuses")
        }
        
        val result = supportRepository.updateTicketStatus(ticketId, newStatus)
        if (result is Resource.Success) {
            // Mock notification hook
            if (newStatus == TicketStatus.RESOLVED) {
                println("NOTIFICATION: Ticket $ticketId resolved. User notified.")
            }
        }
        return result
    }

    suspend fun respondToTicket(
        user: User,
        ticketId: String,
        message: String
    ): Resource<Unit> {
        if (message.isBlank()) return Resource.Error("Response cannot be empty")
        
        val response = TicketResponse(
            ticketId = ticketId,
            senderId = user.userId,
            senderName = user.displayName ?: "User",
            message = message
        )
        return supportRepository.saveResponse(response)
    }

    fun getResponses(ticketId: String): Flow<Resource<List<TicketResponse>>> {
        return supportRepository.getTicketResponses(ticketId)
    }

    suspend fun submitFeedback(
        user: User,
        rating: Int,
        comment: String,
        orderId: String? = null,
        vendorId: String? = null,
        vendorName: String? = null
    ): Resource<Unit> {
        if (rating !in 1..5) return Resource.Error("Rating must be between 1 and 5")
        if (comment.isBlank() && rating < 4) return Resource.Error("Please provide a comment for low ratings")
        
        val feedback = Feedback(
            userId = user.userId,
            userName = user.displayName ?: "Unknown User",
            orderId = orderId,
            vendorId = vendorId,
            vendorName = vendorName,
            rating = rating,
            comment = comment
        )
        
        return supportRepository.saveFeedback(feedback)
    }

    fun getFeedbackList(user: User): Flow<Resource<List<Feedback>>> {
        // Technically anyone could view feedback, or maybe just admins. Let's return all.
        return supportRepository.getAllFeedback()
    }

    fun getVendorFeedback(vendorId: String): Flow<Resource<List<Feedback>>> {
        return supportRepository.getVendorFeedback(vendorId)
    }
}
