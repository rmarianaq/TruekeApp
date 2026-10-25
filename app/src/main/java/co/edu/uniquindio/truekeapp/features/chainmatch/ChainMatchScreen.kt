package co.edu.uniquindio.truekeapp.features.chainmatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.TruekePrimaryButton
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.service.ChainStep
import coil3.compose.AsyncImage

@Composable
fun ChainMatchScreen(
    onBackClick: () -> Unit,
    viewModel: ChainMatchViewModel = hiltViewModel(),
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

    // "Tú" para el usuario actual, el primer nombre para los demás
    val you = stringResource(R.string.chain_you)
    fun nameOf(userId: String): String =
        if (userId == uiState.currentUserId) you else uiState.users[userId]?.name?.substringBefore(" ").orEmpty()

    val chain = uiState.chainMatch

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TruekeTopBar(title = stringResource(R.string.chain_title), onBackClick = onBackClick) },
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
        bottomBar = {
            if (chain != null) {
                Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TruekePrimaryButton(
                            text = stringResource(
                                if (uiState.alreadyProposed) R.string.chain_already_proposed else R.string.chain_propose
                            ),
                            isLoading = uiState.result is RequestResult.Loading,
                            enabled = !uiState.alreadyProposed,
                            onClick = viewModel::proposeChain,
                        )
                        TextButton(onClick = onBackClick) { Text(stringResource(R.string.chain_not_now)) }
                    }
                }
            }
        },
    ) { padding ->
        if (!uiState.isLoading && chain == null) {
            EmptyState(
                icon = Icons.Rounded.LinkOff,
                title = stringResource(R.string.chain_empty_title),
                message = stringResource(R.string.chain_empty_message),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }
        if (chain == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Tarjeta de explicación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.primaryContainer),
                        ),
                    )
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text(
                    text = stringResource(R.string.chain_card_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = stringResource(
                        R.string.chain_card_message,
                        chain.participantIds.filter { it != uiState.currentUserId }.joinToString(" y ") { nameOf(it) },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chain_algorithm),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column {
                Text(
                    text = stringResource(R.string.chain_how_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
                chain.steps.forEachIndexed { index, step ->
                    ChainStepRow(
                        number = index + 1,
                        step = step,
                        giverName = nameOf(step.giverId),
                        receiverName = nameOf(step.receiverId),
                        isGiverYou = step.giverId == uiState.currentUserId,
                        isLast = index == chain.steps.lastIndex,
                    )
                }
            }

            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.chain_reserved, chain.steps.size), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ChainStepRow(
    number: Int,
    step: ChainStep,
    giverName: String,
    receiverName: String,
    isGiverYou: Boolean,
    isLast: Boolean,
) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(number.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(84.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 1.dp,
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = step.publication.imageUrls.firstOrNull(),
                    contentDescription = step.publication.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isGiverYou) stringResource(R.string.chain_you_give) else stringResource(R.string.chain_gives, giverName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(step.publication.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = stringResource(R.string.chain_receives, receiverName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}
