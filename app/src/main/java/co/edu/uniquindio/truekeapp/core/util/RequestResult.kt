package co.edu.uniquindio.truekeapp.core.util

import androidx.annotation.StringRes

// Los 3 estados posibles de una operación: cargando, salió bien o falló
sealed class RequestResult {
    data object Loading : RequestResult()

    data class Success(
        @StringRes val messageRes: Int,
        val args: List<Any> = emptyList(),
    ) : RequestResult()

    data class Failure(@StringRes val messageRes: Int) : RequestResult()
}