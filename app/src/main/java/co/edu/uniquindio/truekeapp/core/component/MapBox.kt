package co.edu.uniquindio.truekeapp.core.component

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import co.edu.uniquindio.truekeapp.R
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

// Coordenadas por defecto: Armenia, Quindío. Ojo: Mapbox usa (longitud, latitud)
val DEFAULT_MAP_CENTER: Point = Point.fromLngLat(-75.6811, 4.5339)

/**
 * Mapa reutilizable (guía 16).
 * - [selectedPoint]: el punto marcado con el pin (la ubicación de la publicación).
 * - [onMapClick]: si no es null, tocar el mapa elige un punto (state hoisting: la pantalla decide).
 * - [interactive] = false: mapa de solo lectura (no captura gestos dentro de un scroll).
 * - [showUserLocation]: pide el permiso de ubicación y muestra el punto azul del usuario.
 */
@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    center: Point = DEFAULT_MAP_CENTER,
    zoom: Double = 13.0,
    selectedPoint: Point? = null,
    onMapClick: ((Point) -> Unit)? = null,
    interactive: Boolean = true,
    showUserLocation: Boolean = false,
) {
    // Sin token el SDK de Mapbox no puede cargar el mapa: se muestra un aviso en vez de fallar
    if (stringResource(R.string.mapbox_access_token).isBlank()) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.map_unavailable), style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val context = LocalContext.current

    // La cámara inicial solo se aplica en la primera composición
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(center)
            zoom(zoom)
        }
    }

    // Permiso de ubicación (se piden FINE y COARSE juntos, guía 15)
    fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var locationGranted by remember { mutableStateOf(hasLocationPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> locationGranted = result.values.any { it } }

    if (showUserLocation) {
        LaunchedEffect(Unit) {
            if (!locationGranted) {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
        // Si el usuario cambia el permiso desde Ajustes y vuelve, se revisa de nuevo
        LifecycleResumeEffect(Unit) {
            locationGranted = hasLocationPermission()
            onPauseOrDispose { }
        }
    }

    val marker = rememberIconImage(
        key = R.drawable.ic_map_marker,
        painter = painterResource(R.drawable.ic_map_marker),
    )

    Box(modifier) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
            onMapClickListener = OnMapClickListener { point ->
                onMapClick?.invoke(point)
                true
            },
        ) {
            // Punto azul con la ubicación del usuario
            if (showUserLocation) {
                MapEffect(locationGranted) { mapView ->
                    mapView.location.updateSettings {
                        locationPuck = createDefault2DPuck(withBearing = true)
                        puckBearingEnabled = true
                        enabled = locationGranted
                    }
                }
            }

            // Pin del punto elegido o de la publicación
            selectedPoint?.let { point ->
                PointAnnotation(point = point) {
                    iconImage = marker
                }
            }
        }

        if (!interactive) {
            // Capa transparente: se "come" los toques para que el mapa no se mueva
            Box(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {}
            )
        } else if (showUserLocation && locationGranted) {
            // Botón "Mi ubicación": la cámara sigue al usuario
            SmallFloatingActionButton(
                onClick = { mapViewportState.transitionToFollowPuckState() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Rounded.MyLocation, stringResource(R.string.map_my_location), Modifier.size(20.dp))
            }
        }
    }
}
