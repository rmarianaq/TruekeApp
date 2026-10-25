package co.edu.uniquindio.truekeapp.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.util.timeAgo
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (publicationId: String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TruekeTopBar(
                title = stringResource(R.string.notifications_title),
                subtitle = stringResource(R.string.notifications_unread, uiState.unreadCount),
                onBackClick = onBackClick,
                actions = {
                    if (uiState.unreadCount > 0) {
                        IconButton(onClick = viewModel::markAllAsRead) {
                            Icon(
                                Icons.Rounded.DoneAll,
                                contentDescription = stringResource(R.string.notifications_mark_all),
                                tint = Color.White,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (!uiState.isLoading && uiState.notifications.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.NotificationsNone,
                        title = stringResource(R.string.notifications_empty_title),
                        message = stringResource(R.string.notifications_empty_message),
                    )
                }
            }
            items(uiState.notifications, key = { it.id }) { notification ->
                NotificationRow(
                    notification = notification,
                    onClick = {
                        viewModel.open(notification)
                        onNotificationClick(notification.publicationId)
                    },
                    onToggleRead = { viewModel.toggleRead(notification) },
                    modifier = Modifier.animateItem(),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

private data class NotificationStyle(val icon: ImageVector, val container: Color, val content: Color)

@Composable
private fun styleFor(type: NotificationType): NotificationStyle {
    val c = MaterialTheme.colorScheme
    return when (type) {
        NotificationType.COMMENT -> NotificationStyle(Icons.AutoMirrored.Rounded.Chat, c.tertiaryContainer, c.onTertiaryContainer)
        NotificationType.PROPOSAL_RECEIVED -> NotificationStyle(Icons.Rounded.SwapHoriz, c.primaryContainer, c.onPrimaryContainer)
        NotificationType.PROPOSAL_ACCEPTED -> NotificationStyle(Icons.Rounded.Check, c.secondaryContainer, c.onSecondaryContainer)
        NotificationType.PROPOSAL_REJECTED -> NotificationStyle(Icons.Rounded.Close, c.errorContainer, c.onErrorContainer)
        NotificationType.PUBLICATION_VERIFIED -> NotificationStyle(Icons.Rounded.Verified, c.secondaryContainer, c.onSecondaryContainer)
        NotificationType.PUBLICATION_REJECTED -> NotificationStyle(Icons.Rounded.Block, c.errorContainer, c.onErrorContainer)
    }
}

@Composable
private fun titleFor(n: Notification): String = stringResource(
    when (n.type) {
        NotificationType.COMMENT -> R.string.notif_comment_title
        NotificationType.PROPOSAL_RECEIVED -> R.string.notif_proposal_title
        NotificationType.PROPOSAL_ACCEPTED -> R.string.notif_accepted_title
        NotificationType.PROPOSAL_REJECTED -> R.string.notif_rejected_title
        NotificationType.PUBLICATION_VERIFIED -> R.string.notif_verified_title
        NotificationType.PUBLICATION_REJECTED -> R.string.notif_pub_rejected_title
    }
)

@Composable
private fun messageFor(n: Notification): String = when (n.type) {
    NotificationType.COMMENT -> stringResource(R.string.notif_comment_message, n.actorName, n.publicationTitle)
    NotificationType.PROPOSAL_RECEIVED -> stringResource(R.string.notif_proposal_message, n.actorName, n.publicationTitle)
    NotificationType.PROPOSAL_ACCEPTED -> stringResource(R.string.notif_accepted_message, n.actorName, n.publicationTitle)
    NotificationType.PROPOSAL_REJECTED -> stringResource(R.string.notif_rejected_message, n.actorName, n.publicationTitle)
    NotificationType.PUBLICATION_VERIFIED -> stringResource(R.string.notif_verified_message, n.publicationTitle)
    NotificationType.PUBLICATION_REJECTED -> stringResource(R.string.notif_pub_rejected_message, n.publicationTitle)
}

@Composable
private fun NotificationRow(
    notification: Notification,
    onClick: () -> Unit,
    onToggleRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = styleFor(notification.type)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (notification.isRead) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            )
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(style.container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(style.icon, contentDescription = null, tint = style.content)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titleFor(notification), style = MaterialTheme.typography.titleSmall)
                if (!notification.isRead) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
            }
            Text(
                messageFor(notification),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                timeAgo(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        IconButton(onClick = onToggleRead) {
            Icon(
                imageVector = if (notification.isRead) Icons.Rounded.MarkEmailUnread else Icons.Rounded.MarkEmailRead,
                contentDescription = stringResource(
                    if (notification.isRead) R.string.notifications_mark_unread else R.string.notifications_mark_read
                ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
