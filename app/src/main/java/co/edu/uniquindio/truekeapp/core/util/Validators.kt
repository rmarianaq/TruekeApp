package co.edu.uniquindio.truekeapp.core.util

import androidx.annotation.StringRes
import co.edu.uniquindio.truekeapp.R

// Validaciones reutilizables en todos los formularios.
// Devuelven null si todo está bien, o el mensaje de error.
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @StringRes
    fun email(value: String): Int? = when {
        value.isBlank() -> R.string.error_email_required
        !EMAIL_REGEX.matches(value.trim()) -> R.string.error_email_invalid
        else -> null
    }

    @StringRes
    fun password(value: String): Int? = when {
        value.isBlank() -> R.string.error_password_required
        value.length < 8 -> R.string.error_password_short
        else -> null
    }
}