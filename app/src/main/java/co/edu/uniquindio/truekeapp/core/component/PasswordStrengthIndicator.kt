package co.edu.uniquindio.truekeapp.core.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.theme.TruekeAmber

@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
) {
    if (password.isEmpty()) return

    // Un punto por cada condición que se cumpla
    val score = listOf(
        password.length >= 8,
        password.any { it.isUpperCase() },
        password.any { it.isDigit() },
        password.any { !it.isLetterOrDigit() },
    ).count { it }

    val colors = MaterialTheme.colorScheme
    val (progress, color, labelRes) = when {
        score <= 1 -> Triple(0.33f, colors.error, R.string.password_strength_weak)
        score <= 3 -> Triple(0.66f, TruekeAmber, R.string.password_strength_medium)
        else -> Triple(1f, colors.secondary, R.string.password_strength_strong)
    }

    // Anima el cambio de largo y de color de la barra
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "strengthProgress")
    val animatedColor by animateColorAsState(targetValue = color, label = "strengthColor")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = animatedColor,
            trackColor = colors.surfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.password_strength_label, stringResource(labelRes)),
            style = MaterialTheme.typography.labelSmall,
            color = animatedColor,
        )
    }
}