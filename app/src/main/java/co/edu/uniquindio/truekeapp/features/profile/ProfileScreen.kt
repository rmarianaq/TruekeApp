package co.edu.uniquindio.truekeapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.MainTab
import co.edu.uniquindio.truekeapp.core.component.TrustBadge
import co.edu.uniquindio.truekeapp.core.component.TruekeBottomBar
import co.edu.uniquindio.truekeapp.core.component.UserAvatar
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.theme.TruekeAmber
import co.edu.uniquindio.truekeapp.core.theme.TruekeOrchid
import co.edu.uniquindio.truekeapp.core.theme.TruekeViolet
import co.edu.uniquindio.truekeapp.core.theme.TruekeVioletDeep

@Composable
fun ProfileScreen(
    onTabSelected: (MainTab) -> Unit,
    onEditProfileClick: () -> Unit,
    onMyPublicationsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    onBadgesClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val user = uiState.user

    LightSystemBarIcons()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = { TruekeBottomBar(selected = MainTab.PROFILE, onTabSelected = onTabSelected) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ─── Encabezado con avatar ───
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
                IconButton(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_profile_title), tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 24.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    UserAvatar(name = user?.name.orEmpty(), size = 84.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(user?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.height(10.dp))
                    TrustBadge(score = uiState.trustScore, onClick = onStatisticsClick)
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ─── Nivel y puntos ───
                if (user != null) {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.profile_level, stringResource(user.level.labelRes)),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = TruekeAmber)
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { uiState.levelProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = uiState.nextLevel?.let {
                                    stringResource(R.string.profile_next_level, user.points, it.minPoints, stringResource(it.labelRes))
                                } ?: stringResource(R.string.profile_max_level),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // ─── Contadores ───
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        value = uiState.publicationCount,
                        label = stringResource(R.string.profile_publications),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        value = uiState.exchangeCount,
                        label = stringResource(R.string.profile_exchanges),
                        modifier = Modifier.weight(1f),
                    )
                }

                // ─── Menú ───
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column {
                        MenuRow(Icons.Rounded.Inventory2, stringResource(R.string.profile_my_publications), onMyPublicationsClick)
                        MenuDivider()
                        MenuRow(Icons.Rounded.SwapHoriz, stringResource(R.string.proposals_title), onClick = { onTabSelected(MainTab.PROPOSALS) })
                        MenuRow(Icons.Rounded.Favorite, stringResource(R.string.favorites_title), onFavoritesClick)
                        MenuDivider()
                        MenuRow(Icons.Rounded.BarChart, stringResource(R.string.statistics_title), onStatisticsClick)
                        MenuDivider()
                        MenuRow(Icons.Rounded.MilitaryTech, stringResource(R.string.badges_title), onBadgesClick)
                        MenuDivider()
                        MenuRow(
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            text = stringResource(R.string.action_logout),
                            onClick = { showLogoutDialog = true },
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_title)) },
            text = { Text(stringResource(R.string.logout_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.action_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun StatCard(value: Int, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (color == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else color)
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
