package co.edu.uniquindio.truekeapp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = lightPrimary, onPrimary = lightOnPrimary,
    primaryContainer = lightPrimaryContainer, onPrimaryContainer = lightOnPrimaryContainer,
    secondary = lightSecondary, onSecondary = lightOnSecondary,
    secondaryContainer = lightSecondaryContainer, onSecondaryContainer = lightOnSecondaryContainer,
    tertiary = lightTertiary, onTertiary = lightOnTertiary,
    tertiaryContainer = lightTertiaryContainer, onTertiaryContainer = lightOnTertiaryContainer,
    error = lightError, onError = lightOnError,
    errorContainer = lightErrorContainer, onErrorContainer = lightOnErrorContainer,
    background = lightBackground, onBackground = lightOnBackground,
    surface = lightSurface, onSurface = lightOnSurface,
    surfaceVariant = lightSurfaceVariant, onSurfaceVariant = lightOnSurfaceVariant,
    outline = lightOutline, outlineVariant = lightOutlineVariant, scrim = lightScrim,
    inverseSurface = lightInverseSurface, inverseOnSurface = lightInverseOnSurface,
    inversePrimary = lightInversePrimary,
    surfaceDim = lightSurfaceDim, surfaceBright = lightSurfaceBright,
    surfaceContainerLowest = lightSurfaceContainerLowest, surfaceContainerLow = lightSurfaceContainerLow,
    surfaceContainer = lightSurfaceContainer, surfaceContainerHigh = lightSurfaceContainerHigh,
    surfaceContainerHighest = lightSurfaceContainerHighest,
)

private val DarkColors = darkColorScheme(
    primary = darkPrimary, onPrimary = darkOnPrimary,
    primaryContainer = darkPrimaryContainer, onPrimaryContainer = darkOnPrimaryContainer,
    secondary = darkSecondary, onSecondary = darkOnSecondary,
    secondaryContainer = darkSecondaryContainer, onSecondaryContainer = darkOnSecondaryContainer,
    tertiary = darkTertiary, onTertiary = darkOnTertiary,
    tertiaryContainer = darkTertiaryContainer, onTertiaryContainer = darkOnTertiaryContainer,
    error = darkError, onError = darkOnError,
    errorContainer = darkErrorContainer, onErrorContainer = darkOnErrorContainer,
    background = darkBackground, onBackground = darkOnBackground,
    surface = darkSurface, onSurface = darkOnSurface,
    surfaceVariant = darkSurfaceVariant, onSurfaceVariant = darkOnSurfaceVariant,
    outline = darkOutline, outlineVariant = darkOutlineVariant, scrim = darkScrim,
    inverseSurface = darkInverseSurface, inverseOnSurface = darkInverseOnSurface,
    inversePrimary = darkInversePrimary,
    surfaceDim = darkSurfaceDim, surfaceBright = darkSurfaceBright,
    surfaceContainerLowest = darkSurfaceContainerLowest, surfaceContainerLow = darkSurfaceContainerLow,
    surfaceContainer = darkSurfaceContainer, surfaceContainerHigh = darkSurfaceContainerHigh,
    surfaceContainerHighest = darkSurfaceContainerHighest,
)

@Composable
fun TruekeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}