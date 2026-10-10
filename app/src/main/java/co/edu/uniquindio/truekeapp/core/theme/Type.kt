package co.edu.uniquindio.truekeapp.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.truekeapp.R

// La fuente de Trueke, con sus 5 grosores
val JakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

// Partimos de los tamaños estándar de Material 3 y les cambiamos la fuente.
// A los títulos grandes les damos más peso para que tengan carácter.
private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    displayMedium = base.displayMedium.copy(fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    displaySmall = base.displaySmall.copy(fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold),
    headlineLarge = base.headlineLarge.copy(fontFamily = JakartaSans, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = JakartaSans, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = JakartaSans, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = JakartaSans, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = JakartaSans),
    bodyMedium = base.bodyMedium.copy(fontFamily = JakartaSans),
    bodySmall = base.bodySmall.copy(fontFamily = JakartaSans),
    labelLarge = base.labelLarge.copy(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = JakartaSans, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = JakartaSans, fontWeight = FontWeight.Medium),
)