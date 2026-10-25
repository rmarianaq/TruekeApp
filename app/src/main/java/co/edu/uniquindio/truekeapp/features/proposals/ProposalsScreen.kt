package co.edu.uniquindio.truekeapp.features.proposals

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.MainTab
import co.edu.uniquindio.truekeapp.core.component.StatusChip
import co.edu.uniquindio.truekeapp.core.component.TruekeBottomBar
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.timeAgo
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import coil3.compose.AsyncImage

@Composable
fun ProposalsScreen(
    onTabSelected: (MainTab) -> Unit,
    onPublicationClick: (String) -> Unit,
    viewModel: ProposalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.result) {
        val result = uiState.result
        if (result is RequestResult.Success) {
            snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = false)
            viewModel.resetResult()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TruekeTopBar(
                title = stringResource(R.string.proposals_title),
                subtitle = stringResource(R.string.proposals_subtitle),
            )
        },
        bottomBar = { TruekeBottomBar(selected = MainTab.PROPOSALS, onTabSelected = onTabSelected) },
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ProposalTab.entries.forEachIndexed { index, tab ->
                        val count = if (tab == ProposalTab.RECEIVED) uiState.received.size else uiState.sent.size
                        SegmentedButton(
                            selected = uiState.tab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ProposalTab.entries.size),
                        ) {
                            Text(
                                stringResource(
                                    if (tab == ProposalTab.RECEIVED) R.string.proposals_received else R.string.proposals_sent,
                                    count,
                                ),
                            )
                        }
                    }
                }
            }
            if (uiState.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Inbox,
                        title = stringResource(R.string.proposals_empty_title),
                        message = stringResource(R.string.proposals_empty_message),
                    )
                }
            }
            items(uiState.items, key = { it.proposal.id }) { item ->
                ProposalCard(
                    item = item,
                    isReceived = uiState.tab == ProposalTab.RECEIVED,
                    onClick = { onPublicationClick(item.proposal.publicationId) },
                    onAccept = { viewModel.accept(item) },
                    onReject = { viewModel.reject(item) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ProposalCard(
    item: ProposalItem,
    isReceived: Boolean,
    onClick: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val (statusText, statusContainer, statusContent) = when (item.proposal.status) {
        ProposalStatus.PENDING -> Triple(stringResource(R.string.status_pending), colors.tertiaryContainer, colors.onTertiaryContainer)
        ProposalStatus.ACCEPTED -> Triple(stringResource(R.string.status_accepted), colors.secondaryContainer, colors.onSecondaryContainer)
        ProposalStatus.REJECTED -> Triple(stringResource(R.string.status_rejected), colors.errorContainer, colors.onErrorContainer)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.publication?.imageUrls?.firstOrNull(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.publication?.title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (isReceived) {
                            stringResource(R.string.proposals_offers, item.otherUserName, item.proposal.offeredItem)
                        } else {
                            stringResource(R.string.proposals_you_offer, item.otherUserName, item.proposal.offeredItem)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(text = statusText, containerColor = statusContainer, contentColor = statusContent)
                        if (item.proposal.isChain) {
                            Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primary)
                            Text(stringResource(R.string.proposals_chain), style = MaterialTheme.typography.labelSmall, color = colors.primary)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(timeAgo(item.proposal.createdAt), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            if (isReceived && item.proposal.status == ProposalStatus.PENDING) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.proposals_reject))
                    }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.proposals_accept))
                    }
                }
            }
        }
    }
}
