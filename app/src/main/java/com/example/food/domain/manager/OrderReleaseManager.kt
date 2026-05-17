package com.example.food.domain.manager

import com.example.food.data.model.Notification
import com.example.food.data.model.NotificationStatus
import com.example.food.data.model.NotificationType
import com.example.food.data.model.OrderStatus
import com.example.food.data.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderReleaseManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    suspend fun releaseOrderToVendor(orderId: String) {
        val orderRef = firestore.collection("orders").document(orderId)
        firestore.runTransaction { transaction ->
            transaction.update(orderRef, "orderStatus", OrderStatus.SENT_TO_VENDOR.name)
        }.await()

        val orderSnapshot = orderRef.get().await()
        val vendorId = orderSnapshot.getString("vendorId") ?: ""
        
        if (vendorId.isNotEmpty()) {
            val vendorSnapshot = firestore.collection("vendors").document(vendorId).get().await()
            val vendorUserId = vendorSnapshot.getString("userId") ?: ""
            if (vendorUserId.isNotEmpty()) {
                val notification = Notification(
                    userId = vendorUserId,
                    title = "New Incoming Order!",
                    message = "You have a new paid order waiting for preparation.",
                    type = NotificationType.ORDER_STATUS_UPDATE,
                    status = NotificationStatus.UNREAD,
                    relatedOrderId = orderId
                )
                notificationRepository.saveNotification(notification)
            }
        }
    }
}
