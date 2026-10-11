package co.edu.uniquindio.truekeapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.truekeapp.features.auth.login.LoginScreen
import co.edu.uniquindio.truekeapp.features.onboarding.OnboardingScreen

private const val TRANSITION_MS = 350

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Onboarding,
        // Transiciones: la nueva pantalla entra desde la derecha
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(TRANSITION_MS),
                targetOffset = { it / 4 },
            ) + fadeOut(tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(TRANSITION_MS),
                initialOffset = { it / 4 },
            ) + fadeIn(tween(TRANSITION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_MS))
        },
    ) {
        composable<MainRoutes.Onboarding> {
            OnboardingScreen(
                onCreateAccountClick = { /* Paso 8: ir al registro */ },
                onLoginClick = { navController.navigate(MainRoutes.Login) },
            )
        }

        composable<MainRoutes.Login> {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { /* Paso 8: ir al registro */ },
                onForgotPasswordClick = { /* Paso 9: recuperar contraseña */ },
                onLoginSuccess = { /* Paso 10: ir al feed */ },
            )
        }
    }
}