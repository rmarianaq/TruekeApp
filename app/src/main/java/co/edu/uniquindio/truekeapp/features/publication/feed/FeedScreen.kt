package co.edu.uniquindio.truekeapp.features.publication.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.EmptyState
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.MainTab
import co.edu.uniquindio.truekeapp.core.component.TruekeBottomBar
import co.edu.uniquindio.truekeapp.core.component.UserAvatar
import co.edu.uniquindio.truekeapp.core.component.icon
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.theme.TruekeOrchid
import co.edu.uniquindio.truekeapp.core.theme.TruekeViolet
import co.edu.uniquindio.truekeapp.core.theme.TruekeVioletDeep
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.service.ChainMatch
import co.edu.uniquindio.truekeapp.domain.service.Suggestion
import co.edu.uniquindio.truekeapp.features.suggestions.reasonText
import coil3.compose.AsyncImage

@Composable
fun FeedScreen(
    onPublicationClick: (String) -> Unit,
    onCreatePublicationClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onChainMatchClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FeedContent(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onPublicationClick = onPublicationClick,
        onInterestClick = viewModel::toggleInterest,
        onCreatePublicationClick = onCreatePublicationClick,
        onNotificationsClick = onNotificationsClick,
        onChainMatchClick = onChainMatchClick,
        onSuggestionsClick = onSuggestionsClick,
        onTabSelected = onTabSelected,
    )
}

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onPublicationClick: (String) -> Unit,
    onInterestClick: (String) -> Unit,
    onCreatePublicationClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onChainMatchClick: () -> Unit,
    onSuggestionsClick: () -> Unit,
    onTabSelected: (MainTab) -> Unit,
) {
    LightSystemBarIcons()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = { TruekeBottomBar(selected = MainTab.HOME, onTabSelected = onTabSelected) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreatePublicationClick,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.feed_publish)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "header") {
                FeedHeader(
                    userName = uiState.userName,
                    query = uiState.query,
                    unreadNotifications = uiState.unreadNotifications,
                    onQueryChange = onQueryChange,
                    onNotificationsClick = onNotificationsClick,
                )
            }

            // Funcionalidad innovadora 1: banner del Match en cadena
            if (uiState.showHighlights && uiState.chainMatch != null) {
                item(key = "chain") {
                    ChainMatchBanner(
                        chainMatch = uiState.chainMatch,
                        onClick = onChainMatchClick,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            item(key = "categories") {
                CategoryFilterRow(
                    selected = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }

            // Funcionalidad innovadora 3: carrusel "Sugerido para ti"
            if (uiState.showHighlights && uiState.suggestions.isNotEmpty()) {
                item(key = "suggestions") {
                    SuggestionsCarousel(
                        suggestions = uiState.suggestions,
                        onSuggestionClick = onPublicationClick,
                        onSeeAllClick = onSuggestionsClick,
                    )
                }
            }

            if (!uiState.isLoading && uiState.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.feed_empty_title),
                        message = stringResource(R.string.feed_empty_message),
                    )
                }
            }

            items(uiState.items, key = { it.publication.id }) { item ->
                PublicationCard(
                    item = item,
                    onClick = { onPublicationClick(item.publication.id) },
                    onInterestClick = { onInterestClick(item.publication.id) },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .animateItem(),
                )
            }
        }
    }
}

@Composable
private fun FeedHeader(
    userName: String,
    query: String,
    unreadNotifications: Int,
    onQueryChange: (String) -> Unit,
    onNotificationsClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(listOf(TruekeVioletDeep, TruekeViolet, TruekeOrchid))),
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-60).dp)
                .background(
                    Brush.radialGradient(listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)),
                    CircleShape,
                ),
        )

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = userName, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.feed_greeting, userName),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.feed_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text(unreadNotifications.toString())
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = stringResource(R.string.notifications_title),
                            tint = Color.White,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                placeholder = { Text(stringResource(R.string.feed_search)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                } else null,
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            )
        }
    }
}

@Composable
private fun ChainMatchBanner(
    chainMatch: ChainMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.primaryContainer),
                    ),
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Link, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.chain_banner_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = stringResource(R.string.chain_banner_message, chainMatch.steps.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun SuggestionsCarousel(
    suggestions: List<Suggestion>,
    onSuggestionClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.suggestions_carousel_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSeeAllClick) {
                Text(stringResource(R.string.action_see_all))
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(suggestions, key = { it.publication.id }) { suggestion ->
                Card(
                    onClick = { onSuggestionClick(suggestion.publication.id) },
                    modifier = Modifier.width(160.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    AsyncImage(
                        model = suggestion.publication.imageUrls.firstOrNull(),
                        contentDescription = suggestion.publication.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = suggestion.publication.title,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = reasonText(suggestion),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selected: Category?,
    onCategorySelected: (Category?) -> Unit,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.category_all)) },
                shape = CircleShape,
                colors = chipColors,
            )
        }
        items(Category.entries) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.labelRes)) },
                leadingIcon = {
                    Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                shape = CircleShape,
                colors = chipColors,
            )
        }
    }
}
