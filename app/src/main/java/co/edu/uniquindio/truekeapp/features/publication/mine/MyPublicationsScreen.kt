package co.edu.uniquindio.truekeapp.features.publication.mine

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.StatusChip
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.util.timeAgo
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import coil3.compose.AsyncImage

@Composable
fun MyPublicationsScreen(
    onBackClick: () -> Unit,
    onPublicationClick: (String) -> Unit,
    viewModel: MyPublicationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TruekeTopBar(title = stringResource(R.string.profile_my_publications), onBackClick = onBackClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!uiState.isLoading && uiState.publications.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Inventory2,
                        title = stringResource(R.string.mine_empty_title),
                        message = stringResource(R.string.mine_empty_message),
                    )
                }
            }
            items(uiState.publications, key = { it.id }) { publication ->
                val colors = MaterialTheme.colorScheme
                val (label, container, content) = when (publication.status) {
                    PublicationStatus.PENDING -> Triple(stringResource(R.string.moderation_tab_pending), colors.tertiaryContainer, colors.onTertiaryContainer)
                    PublicationStatus.VERIFIED -> Triple(stringResource(R.string.moderation_tab_verified), colors.secondaryContainer, colors.onSecondaryContainer)
                    PublicationStatus.FINISHED -> Triple(stringResource(R.string.moderation_finished_label), colors.primaryContainer, colors.onPrimaryContainer)
                    PublicationStatus.REJECTED -> Triple(stringResource(R.string.moderation_tab_rejected), colors.errorContainer, colors.onErrorContainer)
                }
                Card(
                    onClick = { onPublicationClick(publication.id) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = publication.imageUrls.firstOrNull(),
                            contentDescription = publication.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceVariant),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(publication.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${stringResource(publication.category.labelRes)} · ${timeAgo(publication.createdAt)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(6.dp))
                            StatusChip(text = label, containerColor = container, contentColor = content)
                            if (publication.status == PublicationStatus.REJECTED && publication.rejectionReason != null) {
                                Text(
                                    stringResource(R.string.moderation_reason, publication.rejectionReason),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.error,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}
