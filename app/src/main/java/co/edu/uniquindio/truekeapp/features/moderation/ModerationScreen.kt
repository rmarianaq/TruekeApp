package co.edu.uniquindio.truekeapp.features.moderation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.StatusChip
import co.edu.uniquindio.truekeapp.core.component.TrustBadge
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.timeAgo
import co.edu.uniquindio.truekeapp.domain.model.Publication
import coil3.compose.AsyncImage

@StringRes
private fun ModerationTab.tabLabelRes(): Int = when (this) {
    ModerationTab.PENDING -> R.string.moderation_tab_pending
    ModerationTab.VERIFIED -> R.string.moderation_tab_verified
    ModerationTab.FINISHED -> R.string.moderation_tab_finished
    ModerationTab.REJECTED -> R.string.moderation_tab_rejected
}

@Composable
fun ModerationScreen(
    onLogout: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var rejecting by remember { mutableStateOf<Publication?>(null) }

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                rejecting = null
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = false)
                viewModel.resetResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = true)
                viewModel.resetResult()
            }
            else -> Unit
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TruekeTopBar(
                title = stringResource(R.string.moderation_title),
                subtitle = stringResource(R.string.moderation_subtitle, uiState.moderatorName),
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = stringResource(R.string.action_logout), tint = Color.White)
                    }
                },
            )
        },
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(ModerationTab.entries) { tab ->
                        FilterChip(
                            selected = uiState.tab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            label = { Text("${stringResource(tab.tabLabelRes())} (${uiState.counts[tab] ?: 0})") },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
            if (uiState.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Inbox,
                        title = stringResource(R.string.moderation_empty_title),
                        message = stringResource(R.string.moderation_empty_message),
                    )
                }
            }
            items(uiState.items, key = { it.publication.id }) { item ->
                ModerationCard(
                    item = item,
                    onVerify = { viewModel.verify(item.publication) },
                    onReject = { rejecting = item.publication },
                    onFinish = { viewModel.markFinished(item.publication) },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .animateItem(),
                )
            }
        }
    }

    rejecting?.let { publication ->
        RejectDialog(
            onDismiss = { rejecting = null },
            onConfirm = { reason -> viewModel.reject(publication, reason) },
        )
    }
}

@Composable
private fun ModerationCard(
    item: ModerationItem,
    onVerify: () -> Unit,
    onReject: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val publication = item.publication
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row {
                AsyncImage(
                    model = publication.imageUrls.firstOrNull(),
                    contentDescription = publication.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(publication.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${stringResource(publication.category.labelRes)} · ${stringResource(publication.condition.labelRes)} · ${timeAgo(publication.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.moderation_published_by, item.ownerName),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(score = item.ownerTrustScore)
                        if (publication.reportedByUserIds.isNotEmpty()) {
                            Surface(shape = CircleShape, color = colors.errorContainer, contentColor = colors.onErrorContainer) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        stringResource(R.string.moderation_reports, publication.reportedByUserIds.size),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(publication.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(12.dp))
            when (item.publication.status) {
                co.edu.uniquindio.truekeapp.domain.model.PublicationStatus.PENDING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                        ) {
                            Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.moderation_reject))
                        }
                        Button(onClick = onVerify, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.moderation_verify))
                        }
                    }
                }
                co.edu.uniquindio.truekeapp.domain.model.PublicationStatus.VERIFIED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(stringResource(R.string.moderation_tab_verified), colors.secondaryContainer, colors.onSecondaryContainer)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onFinish) {
                            Icon(Icons.Rounded.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.moderation_mark_finished))
                        }
                    }
                }
                co.edu.uniquindio.truekeapp.domain.model.PublicationStatus.FINISHED -> {
                    StatusChip(stringResource(R.string.moderation_finished_label), colors.primaryContainer, colors.onPrimaryContainer)
                }
                co.edu.uniquindio.truekeapp.domain.model.PublicationStatus.REJECTED -> {
                    Surface(shape = MaterialTheme.shapes.medium, color = colors.errorContainer, contentColor = colors.onErrorContainer) {
                        Text(
                            stringResource(R.string.moderation_reason, publication.rejectionReason.orEmpty()),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var reason by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Block, contentDescription = null) },
        title = { Text(stringResource(R.string.moderation_reject_title)) },
        text = {
            Column {
                Text(stringResource(R.string.moderation_reject_message), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.moderation_reason_field)) },
                    minLines = 2,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.moderation_reject_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
