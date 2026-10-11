package co.edu.uniquindio.truekeapp.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.truekeapp.R

// Convierte una fecha en "Hace 5 min", "Hace 2 h", "Hace 3 d"
@Composable
fun timeAgo(timestamp: Long): String {
    val minutes = (System.currentTimeMillis() - timestamp) / 60_000
    return when {
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_minutes_ago, minutes.toInt())
        minutes < 60 * 24 -> stringResource(R.string.time_hours_ago, (minutes / 60).toInt())
        else -> stringResource(R.string.time_days_ago, (minutes / (60 * 24)).toInt())
    }
}