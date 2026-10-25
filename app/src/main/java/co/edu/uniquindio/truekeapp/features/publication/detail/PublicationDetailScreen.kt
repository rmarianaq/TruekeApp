package co.edu.uniquindio.truekeapp.features.publication.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.TrustBadge
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.UserAvatar
import co.edu.uniquindio.truekeapp.core.component.icon
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.theme.TruekeHeart
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.distanceText
import co.edu.uniquindio.truekeapp.core.util.timeAgo
import co.edu.uniquindio.truekeapp.domain.model.User
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun PublicationDetailScreen(
    onBackClick: () -> Unit,
    onCommentsClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    viewModel: PublicationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showProposeDialog by rememberSaveable { mutableStateOf(false) }
    var showReportDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                showProposeDialog = false
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

    val publication = uiState.publication

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
        bottomBar = {
            if (publication != null) {
                DetailBottomBar(
                    uiState = uiState,
                    onEditClick = { onEditClick(publication.id) },
                    onReportClick = { showReportDialog = true },
                    onProposeClick = { showProposeDialog = true },
                )
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            publication == null -> NotFoundContent(onBackClick = onBackClick)
            else -> DetailBody(
                uiState = uiState,
                onBackClick = onBackClick,
                onInterestClick = viewModel::toggleInterest,
                onCommentsClick = { onCommentsClick(publication.id) },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showProposeDialog) {
        ProposeDialog(
            isLoading = uiState.result is RequestResult.Loading,
            onDismiss = { showProposeDialog = false },
            onConfirm = viewModel::propose,
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = { Icon(Icons.Rounded.Flag, contentDescription = null) },
            title = { Text(stringResource(R.string.detail_report_title)) },
            text = { Text(stringResource(R.string.detail_report_message)) },
            confirmButton = {
                Button(onClick = {
                    showReportDialog = false
                    viewModel.report()
                }) { Text(stringResource(R.string.detail_report)) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailBody(
    uiState: PublicationDetailUiState,
    onBackClick: () -> Unit,
    onInterestClick: () -> Unit,
    onCommentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val publication = uiState.publication ?: return
    LightSystemBarIcons()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── Foto principal con botones flotantes ───
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(publication.imageUrls.firstOrNull())
                    .crossfade(true)
                    .build(),
                contentDescription = publication.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent))),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBackClick,
                )
                if (!uiState.isMine) {
                    CircleIconButton(
                        icon = if (uiState.isInterested) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.action_interest),
                        onClick = onInterestClick,
                        tint = if (uiState.isInterested) TruekeHeart else Color.White,
                    )
                }
            }
        }

        // ─── Hoja de contenido que se monta sobre la foto ───
        Column(
            modifier = Modifier
                .offset(y = (-32).dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPill(
                    icon = publication.category.icon,
                    text = stringResource(publication.category.labelRes),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                InfoPill(
                    icon = Icons.Rounded.AutoAwesome,
                    text = stringResource(publication.condition.labelRes),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(text = publication.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = listOfNotNull(
                    timeAgo(publication.createdAt),
                    uiState.distanceKm?.let { distanceText(it) },
                    pluralStringResource(R.plurals.feed_interested, publication.interestCount, publication.interestCount),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            uiState.owner?.let { owner ->
                Spacer(Modifier.height(24.dp))
                OwnerCard(owner = owner, trustScore = uiState.ownerTrustScore)
            }

            Spacer(Modifier.height(12.dp))
            CommentsCard(count = uiState.commentCount, onClick = onCommentsClick)

            Spacer(Modifier.height(28.dp))
            SectionTitle(text = stringResource(R.string.detail_description))
            Text(
                text = publication.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))
            SectionTitle(text = stringResource(R.string.detail_wants))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                publication.wantedItems.forEach { item ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle(text = stringResource(R.string.detail_location))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(text = publication.address, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
        }
    }
}

@Composable
private fun InfoPill(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun OwnerCard(owner: User, trustScore: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(name = owner.name, size = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = owner.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(
                        R.string.detail_owner_level,
                        stringResource(owner.level.labelRes),
                        owner.points,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                // Funcionalidad innovadora 2: Score de confianza
                TrustBadge(score = trustScore)
            }
        }
    }
}

@Composable
private fun CommentsCard(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.detail_comments, count),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun DetailBottomBar(
    uiState: PublicationDetailUiState,
    onEditClick: () -> Unit,
    onReportClick: () -> Unit,
    onProposeClick: () -> Unit,
) {
    Surface(
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isMine) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_edit), style = MaterialTheme.typography.titleSmall)
                }
            } else {
                OutlinedButton(
                    onClick = onReportClick,
                    enabled = !uiState.isReportedByMe,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(
                        stringResource(if (uiState.isReportedByMe) R.string.detail_reported else R.string.detail_report),
                    )
                }
                Button(
                    onClick = onProposeClick,
                    enabled = !uiState.hasPendingProposal,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            if (uiState.hasPendingProposal) R.string.detail_proposal_sent else R.string.detail_propose
                        ),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProposeDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var offered by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.SwapHoriz, contentDescription = null) },
        title = { Text(stringResource(R.string.propose_title)) },
        text = {
            Column {
                Text(stringResource(R.string.propose_message), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = offered,
                    onValueChange = { offered = it },
                    label = { Text(stringResource(R.string.propose_field)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(offered) }, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.propose_send))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Composable
private fun NotFoundContent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.detail_not_found),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.action_back))
        }
    }
}
