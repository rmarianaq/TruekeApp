package co.edu.uniquindio.truekeapp.features.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.StatusChip
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.UserAvatar
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.timeAgo

@Composable
fun CommentsScreen(
    onBackClick: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                if (uiState.comments.isNotEmpty()) listState.animateScrollToItem(uiState.comments.lastIndex)
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
                title = stringResource(R.string.comments_title),
                subtitle = stringResource(R.string.comments_about, uiState.publicationTitle),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
        bottomBar = {
            CommentInput(
                userName = uiState.currentUserName,
                draft = uiState.draft,
                onDraftChange = viewModel::onDraftChange,
                onSend = viewModel::send,
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (uiState.comments.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        title = stringResource(R.string.comments_empty_title),
                        message = stringResource(R.string.comments_empty_message),
                    )
                }
            }
            items(uiState.comments, key = { it.comment.id }) { item ->
                CommentRow(item = item, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun CommentRow(item: CommentItem, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        UserAvatar(name = item.authorName, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.authorName, style = MaterialTheme.typography.titleSmall)
                if (item.isOwner) {
                    Spacer(Modifier.width(6.dp))
                    StatusChip(
                        text = stringResource(R.string.comments_owner),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    timeAgo(item.comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(4.dp))
            Surface(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = if (item.isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = item.comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun CommentInput(
    userName: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(name = userName, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            TextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.comments_placeholder)) },
                shape = CircleShape,
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = stringResource(R.string.comments_send))
            }
        }
    }
}
