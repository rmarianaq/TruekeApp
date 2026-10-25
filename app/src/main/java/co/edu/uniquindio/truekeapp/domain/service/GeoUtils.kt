package co.edu.uniquindio.truekeapp.domain.service

import co.edu.uniquindio.truekeapp.domain.model.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    // Distancia en línea recta entre dos puntos del planeta (fórmula de Haversine)
    fun distanceKm(a: Location, b: Location): Double {
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_KM * atan2(sqrt(h), sqrt(1 - h))
    }
}
