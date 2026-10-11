package co.edu.uniquindio.truekeapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.truekeapp.features.auth.login.LoginScreen
import co.edu.uniquindio.truekeapp.features.auth.register.RegisterScreen
import co.edu.uniquindio.truekeapp.features.onboarding.OnboardingScreen
import co.edu.uniquindio.truekeapp.features.auth.recoverpass.RecoverPasswordScreen

private const val TRANSITION_MS = 350

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Onboarding,
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
                onCreateAccountClick = { navController.navigate(MainRoutes.Register) },
                onLoginClick = { navController.navigate(MainRoutes.Login) },
            )
        }

        composable<MainRoutes.Login> {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterClick = { navController.navigate(MainRoutes.Register) },
                onForgotPasswordClick = { navController.navigate(MainRoutes.RecoverPassword) },
                onLoginSuccess = { /* Paso 10: ir al feed */ },
            )
        }

        composable<MainRoutes.Register> {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.goToLogin() },
                onRegisterSuccess = { navController.goToLogin() },
            )
        }

        composable<MainRoutes.RecoverPassword> {
            RecoverPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onBackToLoginClick = { navController.popBackStack() },
            )
        }
    }
}

// Va al login dejando solo el onboarding detrás.
// Así "atrás" desde el login nunca vuelve a un registro ya completado.
private fun NavHostController.goToLogin() {
    navigate(MainRoutes.Login) {
        popUpTo(MainRoutes.Onboarding)
        launchSingleTop = true
    }
}