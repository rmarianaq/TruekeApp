package co.edu.uniquindio.truekeapp.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Esquinas generosas = apariencia moderna y amigable
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips pequeños
    small = RoundedCornerShape(12.dp),       // campos de texto
    medium = RoundedCornerShape(16.dp),      // tarjetas pequeñas
    large = RoundedCornerShape(24.dp),       // tarjetas del feed
    extraLarge = RoundedCornerShape(32.dp),  // hojas inferiores y diálogos
)