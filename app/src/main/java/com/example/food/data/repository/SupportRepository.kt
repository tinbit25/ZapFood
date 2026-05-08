package com.example.food.data.repository

import com.example.food.core.util.Resource
import com.example.food.data.model.*
import com.example.food.domain.repository.ISupportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
class SupportRepository : ISupportRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    
    private val ticketsCollection = firestore.collection("support_tickets")
    private val responsesCollection = firestore.collection("ticket_responses")
    private val feedbackCollection = firestore.collection("feedback")

    override suspend fun saveTicket(ticket: SupportTicket): Resource<Unit> {
        return try {
            ticketsCollection.document(ticket.ticketId).set(ticket).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save ticket")
        }
    }

    override fun getUserTickets(userId: String): Flow<Resource<List<SupportTicket>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ticketsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch tickets"))
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { it.toObject(SupportTicket::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(tickets))
            }
        awaitClose { listener.remove() }
    }

    override fun getAllTickets(): Flow<Resource<List<SupportTicket>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = ticketsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch tickets"))
                    return@addSnapshotListener
                }
                val tickets = snapshot?.documents?.mapNotNull { it.toObject(SupportTicket::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(tickets))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateTicketStatus(ticketId: String, newStatus: TicketStatus): Resource<Unit> {
        return try {
            ticketsCollection.document(ticketId)
                .update(
                    mapOf(
                        "status" to newStatus.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update ticket status")
        }
    }

    override suspend fun saveResponse(response: TicketResponse): Resource<Unit> {
        return try {
            responsesCollection.document(response.responseId).set(response).await()
            
            // Also update the ticket's updatedAt timestamp
            ticketsCollection.document(response.ticketId)
                .update("updatedAt", System.currentTimeMillis())
                .await()
                
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save response")
        }
    }

    override fun getTicketResponses(ticketId: String): Flow<Resource<List<TicketResponse>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = responsesCollection
            .whereEqualTo("ticketId", ticketId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch responses"))
                    return@addSnapshotListener
                }
                val responses = snapshot?.documents?.mapNotNull { it.toObject(TicketResponse::class.java) }
                    ?.sortedBy { it.timestamp } ?: emptyList()
                trySend(Resource.Success(responses))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveFeedback(feedback: Feedback): Resource<Unit> {
        return try {
            feedbackCollection.document(feedback.feedbackId).set(feedback).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save feedback")
        }
    }

    override fun getAllFeedback(): Flow<Resource<List<Feedback>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = feedbackCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Failed to fetch feedback"))
                    return@addSnapshotListener
                }
                val feedbackList = snapshot?.documents?.mapNotNull { it.toObject(Feedback::class.java) }
                    ?.sortedByDescending { it.createdAt } ?: emptyList()
                trySend(Resource.Success(feedbackList))
            }
        awaitClose { listener.remove() }
    }
}
