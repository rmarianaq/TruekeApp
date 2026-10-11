package co.edu.uniquindio.truekeapp.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.TruekeLogo
import co.edu.uniquindio.truekeapp.core.theme.TruekeAmber
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import co.edu.uniquindio.truekeapp.core.theme.TruekeOrchid
import co.edu.uniquindio.truekeapp.core.theme.TruekeViolet
import co.edu.uniquindio.truekeapp.core.theme.TruekeVioletDeep

@Composable
fun OnboardingScreen(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    LightSystemBarIcons()

    // Arranca en false y pasa a true apenas se muestra la pantalla: eso dispara las animaciones
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(TruekeVioletDeep, TruekeViolet))),
    ) {
        // ─── Brillos decorativos de fondo ───
        Box(
            modifier = Modifier
                .size(340.dp)
                .offset(x = 150.dp, y = (-90).dp)
                .background(
                    Brush.radialGradient(listOf(TruekeOrchid.copy(alpha = 0.55f), Color.Transparent)),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(TruekeAmber.copy(alpha = 0.22f), Color.Transparent)),
                    CircleShape,
                ),
        )

        // ─── Contenido ───
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.7f),
            ) {
                TruekeLogo(size = 88.dp)
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(visible = visible, enter = enterFromBelow(delay = 150)) {
                Column {
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.onboarding_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(visible = visible, enter = enterFromBelow(delay = 300)) {
                FeaturesCard()
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(visible = visible, enter = enterFromBelow(delay = 450)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCreateAccountClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = TruekeVioletDeep,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_create_account),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    TextButton(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_login),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta "de vidrio" con los 3 beneficios de la app
@Composable
private fun FeaturesCard() {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.10f), shape)
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FeatureRow(number = "1", text = stringResource(R.string.onboarding_feature_publish))
        FeatureRow(number = "2", text = stringResource(R.string.onboarding_feature_find))
        FeatureRow(number = "3", text = stringResource(R.string.onboarding_feature_exchange))
    }
}

@Composable
private fun FeatureRow(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(TruekeAmber, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = TruekeVioletDeep,
            )
        }
        Spacer(Modifier.size(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
        )
    }
}

// Animación reutilizable: aparece y sube un poco, con cierto retraso
private fun enterFromBelow(delay: Int): EnterTransition =
    fadeIn(tween(durationMillis = 700, delayMillis = delay)) +
            slideInVertically(tween(durationMillis = 700, delayMillis = delay)) { it / 4 }

@Preview(showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    TruekeAppTheme {
        OnboardingScreen(onCreateAccountClick = {}, onLoginClick = {})
    }
}