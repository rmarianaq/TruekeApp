package co.edu.uniquindio.truekeapp.core.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Un Snackbar que además sabe si es de éxito o de error
data class TruekeSnackbarVisuals(
    override val message: String,
    val isError: Boolean,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

suspend fun SnackbarHostState.showTruekeSnackbar(message: String, isError: Boolean) {
    currentSnackbarData?.dismiss()   // si había uno visible, lo cierra primero
    showSnackbar(TruekeSnackbarVisuals(message = message, isError = isError))
}

@Composable
fun TruekeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .navigationBarsPadding()   // por encima de la barra de gestos
            .imePadding(),             // y por encima del teclado si está abierto
    ) { data ->
        val isError = (data.visuals as? TruekeSnackbarVisuals)?.isError == true
        val colors = MaterialTheme.colorScheme

        Snackbar(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.medium,
            containerColor = if (isError) colors.errorContainer else colors.secondaryContainer,
            contentColor = if (isError) colors.onErrorContainer else colors.onSecondaryContainer,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                )
                Spacer(Modifier.width(12.dp))
                Text(text = data.visuals.message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}