package co.edu.uniquindio.truekeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import co.edu.uniquindio.truekeapp.features.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TruekeAppTheme {
                // Temporal: cuando tengamos login y registro, aquí va la navegación
                OnboardingScreen(
                    onCreateAccountClick = {},
                    onLoginClick = {},
                )
            }
        }
    }
}