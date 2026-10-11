package co.edu.uniquindio.truekeapp.core.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.theme.TruekeOrchid
import co.edu.uniquindio.truekeapp.core.theme.TruekeViolet
import co.edu.uniquindio.truekeapp.core.theme.TruekeVioletDeep

@Composable
fun TruekeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    val shape = RoundedCornerShape(size * 0.3f)

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 20.dp, shape = shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(TruekeOrchid, TruekeViolet, TruekeVioletDeep)))
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_trueke_logo_foreground),
            contentDescription = stringResource(R.string.logo_content_description),
            modifier = Modifier
                .fillMaxSize()
                .scale(1.6f),
        )
    }
}

@Preview
@Composable
private fun TruekeLogoPreview() {
    TruekeLogo(size = 96.dp)
}