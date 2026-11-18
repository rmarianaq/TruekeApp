package co.edu.uniquindio.truekeapp.core.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.domain.model.Location
import coil3.compose.AsyncImage
import com.mapbox.geojson.Point

/**
 * Foto de la publicación. Usa el Photo Picker del sistema, que no necesita permisos.
 * [image] puede ser la URL de Cloudinary (al editar) o el content:// recién elegido.
 */
@Composable
fun PhotoPickerField(
    image: String?,
    onImagePicked: (String) -> Unit,
    errorRes: Int?,
    enabled: Boolean,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onImagePicked(it.toString()) }
    }
    val openPicker = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(MaterialTheme.shapes.large)
            .background(
                if (errorRes != null) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = enabled, onClick = openPicker),
        contentAlignment = Alignment.Center,
    ) {
        if (image == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AddPhotoAlternate, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.photo_empty), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.photo_pick), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        } else {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            AssistChip(
                onClick = openPicker,
                enabled = enabled,
                label = { Text(stringResource(R.string.photo_change)) },
                leadingIcon = { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            )
        }
    }
    errorRes?.let {
        Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

/** Selector del punto de intercambio en el mapa (guía 16: selectedPoint + onMapClick). */
@Composable
fun LocationPickerField(
    location: Location?,
    initialCenter: Location?,
    onLocationPicked: (Location) -> Unit,
) {
    Text(stringResource(R.string.map_pick_title), style = MaterialTheme.typography.titleSmall)
    Text(
        stringResource(R.string.map_pick_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val start = location ?: initialCenter
    MapBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 8.dp)
            .clip(MaterialTheme.shapes.large),
        center = start?.toPoint() ?: DEFAULT_MAP_CENTER,
        zoom = 14.0,
        selectedPoint = location?.toPoint(),
        onMapClick = { point -> onLocationPicked(Location(latitude = point.latitude(), longitude = point.longitude())) },
        showUserLocation = true,
    )
}

// Mapbox trabaja con (longitud, latitud); el dominio con (latitud, longitud)
fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)
