package co.edu.uniquindio.truekeapp.core.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.truekeapp.R

// Las 3 pestañas de la barra inferior del mockup: Inicio, Propuestas y Perfil
enum class MainTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Rounded.Home),
    PROPOSALS(R.string.tab_proposals, Icons.Rounded.SwapHoriz),
    PROFILE(R.string.tab_profile, Icons.Rounded.Person),
}

@Composable
fun TruekeBottomBar(
    selected: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { if (tab != selected) onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
