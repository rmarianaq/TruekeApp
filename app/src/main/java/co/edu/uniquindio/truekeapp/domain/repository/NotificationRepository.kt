package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.Notification
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val notifications: StateFlow<List<Notification>>
    suspend fun add(notification: Notification)
    suspend fun setRead(id: String, isRead: Boolean)
    suspend fun markAllAsRead(userId: String)
}
