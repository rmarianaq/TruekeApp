package co.edu.uniquindio.truekeapp.features.progress

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.TrustBadge
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.theme.TruekeAmber

// Pestañas "Estadísticas | Insignias" del mockup
@Composable
private fun ProgressTabs(selectedIndex: Int, onStatisticsClick: () -> Unit, onBadgesClick: () -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = selectedIndex == 0,
            onClick = { if (selectedIndex != 0) onStatisticsClick() },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) { Text(stringResource(R.string.statistics_title)) }
        SegmentedButton(
            selected = selectedIndex == 1,
            onClick = { if (selectedIndex != 1) onBadgesClick() },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) { Text(stringResource(R.string.badges_title)) }
    }
}

@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    onBadgesClick: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user
    val colors = MaterialTheme.colorScheme

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TruekeTopBar(title = stringResource(R.string.statistics_title), onBackClick = onBackClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ProgressTabs(selectedIndex = 0, onStatisticsClick = {}, onBadgesClick = onBadgesClick)

            // Contadores por estado
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CounterCard(uiState.active, stringResource(R.string.stats_active), colors.primary, Modifier.weight(1f))
                CounterCard(uiState.finished, stringResource(R.string.stats_finished), colors.secondary, Modifier.weight(1f))
                CounterCard(uiState.pending, stringResource(R.string.stats_pending), colors.tertiary, Modifier.weight(1f))
            }

            // Publicaciones por categoría
            SectionCard(title = stringResource(R.string.stats_by_category)) {
                val max = uiState.byCategory.maxOfOrNull { it.second } ?: 1
                if (uiState.byCategory.isEmpty()) {
                    Text(stringResource(R.string.stats_no_data), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                uiState.byCategory.forEach { (category, count) ->
                    BarRow(label = stringResource(category.labelRes), progress = count.toFloat() / max, value = count.toString())
                }
            }

            // Nivel actual
            if (user != null) {
                SectionCard(title = stringResource(R.string.stats_current_level)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(user.level.labelRes), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        Surface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                            Text(
                                stringResource(R.string.stats_points, user.points),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            )
                        }
                    }
                    val next = uiState.nextLevel
                    if (next != null) {
                        val start = user.level.minPoints
                        LinearProgressIndicator(
                            progress = { ((user.points - start).toFloat() / (next.minPoints - start)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            trackColor = colors.surfaceVariant,
                        )
                        Text(
                            stringResource(R.string.stats_points_missing, next.minPoints - user.points, stringResource(next.labelRes)),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    } else {
                        Text(stringResource(R.string.profile_max_level), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Funcionalidad innovadora 2: desglose del Score de confianza
            SectionCard(title = stringResource(R.string.stats_trust_title), trailing = { TrustBadge(score = uiState.trust.total) }) {
                Text(stringResource(R.string.stats_trust_message), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                BarRow(stringResource(R.string.trust_responsiveness), uiState.trust.responsiveness / 100f, "${uiState.trust.responsiveness}%")
                BarRow(stringResource(R.string.trust_completion), uiState.trust.completion / 100f, "${uiState.trust.completion}%")
                BarRow(stringResource(R.string.trust_reputation), uiState.trust.reputation / 100f, "${uiState.trust.reputation}%")
            }
        }
    }
}

@Composable
fun BadgesScreen(
    onBackClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TruekeTopBar(title = stringResource(R.string.badges_title), onBackClick = onBackClick) },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ProgressTabs(selectedIndex = 1, onStatisticsClick = onStatisticsClick, onBadgesClick = {})
                    Text(
                        stringResource(R.string.badges_summary, uiState.unlockedBadges, uiState.badges.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(uiState.badges) { badge -> BadgeCard(badge) }
        }
    }
}

@Composable
private fun BadgeCard(badge: BadgeItem) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (badge.unlocked) colors.surfaceContainerLowest else colors.surfaceContainerLow,
        shadowElevation = if (badge.unlocked) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(if (badge.unlocked) colors.tertiaryContainer else colors.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (badge.unlocked) Icons.Rounded.MilitaryTech else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (badge.unlocked) TruekeAmber else colors.outline,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(badge.nameRes), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Text(
                stringResource(badge.hintRes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(if (badge.unlocked) R.string.badge_unlocked else R.string.badge_locked),
                style = MaterialTheme.typography.labelMedium,
                color = if (badge.unlocked) colors.secondary else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CounterCard(value: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                trailing()
            }
            content()
        }
    }
}

@Composable
private fun BarRow(label: String, progress: Float, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(130.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.width(10.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
    }
}
