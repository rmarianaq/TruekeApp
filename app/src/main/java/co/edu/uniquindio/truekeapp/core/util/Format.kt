package co.edu.uniquindio.truekeapp.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.truekeapp.R

// "0,8 km" o "12 km" según la distancia
@Composable
fun distanceText(km: Double): String =
    if (km < 10) stringResource(R.string.distance_km_decimal, km) else stringResource(R.string.distance_km, km.toInt())
