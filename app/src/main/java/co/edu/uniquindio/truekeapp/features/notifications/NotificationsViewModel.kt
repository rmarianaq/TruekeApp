package co.edu.uniquindio.truekeapp.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // Se consultan al abrir la pantalla (la guía no pide notificaciones en tiempo real)
    val uiState: StateFlow<NotificationsUiState> = combine(
        notificationRepository.notifications,
        userRepository.currentUser,
    ) { notifications, user ->
        NotificationsUiState(
            notifications = notifications
                .filter { it.userId == user?.id }
                .sortedByDescending { it.createdAt },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    // Al abrir una notificación se marca como leída
    fun open(notification: Notification) {
        viewModelScope.launch { notificationRepository.setRead(notification.id, true) }
    }

    // La guía pide poder marcarlas como leídas o como no leídas
    fun toggleRead(notification: Notification) {
        viewModelScope.launch { notificationRepository.setRead(notification.id, !notification.isRead) }
    }

    fun markAllAsRead() {
        val userId = userRepository.currentUser.value?.id ?: return
        viewModelScope.launch { notificationRepository.markAllAsRead(userId) }
    }
}
