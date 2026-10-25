package co.edu.uniquindio.truekeapp.core.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.R

// Insignia "Confianza 91%" del Score de confianza
@Composable
fun TrustBadge(
    score: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.trust_badge, score),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
    val container = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = CircleShape, color = container, contentColor = contentColor) { content() }
    } else {
        Surface(modifier = modifier, shape = CircleShape, color = container, contentColor = contentColor) { content() }
    }
}
