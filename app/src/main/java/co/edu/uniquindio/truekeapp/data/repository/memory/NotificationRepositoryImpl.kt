package co.edu.uniquindio.truekeapp.data.repository.memory

import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _notifications = MutableStateFlow(seedNotifications())
    override val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    override suspend fun add(notification: Notification) {
        _notifications.update { listOf(notification) + it }
    }

    override suspend fun setRead(id: String, isRead: Boolean) {
        _notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = isRead) else it } }
    }

    override suspend fun markAllAsRead(userId: String) {
        _notifications.update { list -> list.map { if (it.userId == userId) it.copy(isRead = true) else it } }
    }

    private fun seedNotifications(): List<Notification> {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        return listOf(
            Notification("n1", "u1", NotificationType.PROPOSAL_RECEIVED, "p3", "Audífonos inalámbricos", "Laura Pérez", false, now - 5 * minute),
            Notification("n2", "u1", NotificationType.COMMENT, "p3", "Audífonos inalámbricos", "Carlos Gómez", false, now - 180 * minute),
            Notification("n3", "u1", NotificationType.PUBLICATION_VERIFIED, "p3", "Audífonos inalámbricos", "", true, now - 1_500 * minute),
            Notification("n4", "u1", NotificationType.PROPOSAL_REJECTED, "p4", "Chaqueta de jean talla M", "Laura Pérez", true, now - 2_900 * minute),
        )
    }
}
