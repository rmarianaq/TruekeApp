package co.edu.uniquindio.truekeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import co.edu.uniquindio.truekeapp.domain.model.UserRole
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.navigation.AppNavigation
import co.edu.uniquindio.truekeapp.navigation.MainRoutes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TruekeAppTheme {
                val restored by userRepository.sessionRestored.collectAsStateWithLifecycle()
                if (!restored) {
                    // Mientras Firebase revisa si hay una sesión guardada
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                } else {
                    // Sesión persistente: si ya había iniciado sesión, entra directo
                    val start: Any = remember {
                        val user = userRepository.currentUser.value
                        when {
                            user == null -> MainRoutes.Onboarding
                            user.role == UserRole.MODERATOR -> MainRoutes.Moderation
                            else -> MainRoutes.Feed
                        }
                    }
                    AppNavigation(startDestination = start)
                }
            }
        }
    }
}
