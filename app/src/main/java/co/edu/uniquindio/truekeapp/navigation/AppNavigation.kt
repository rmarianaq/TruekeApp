package co.edu.uniquindio.truekeapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.truekeapp.core.component.MainTab
import co.edu.uniquindio.truekeapp.features.auth.login.LoginScreen
import co.edu.uniquindio.truekeapp.features.auth.recoverpass.RecoverPasswordScreen
import co.edu.uniquindio.truekeapp.features.auth.register.RegisterScreen
import co.edu.uniquindio.truekeapp.features.chainmatch.ChainMatchScreen
import co.edu.uniquindio.truekeapp.features.comments.CommentsScreen
import co.edu.uniquindio.truekeapp.features.favorites.FavoritesScreen
import co.edu.uniquindio.truekeapp.features.moderation.ModerationScreen
import co.edu.uniquindio.truekeapp.features.moderation.ModeratorLoginScreen
import co.edu.uniquindio.truekeapp.features.profile.edit.EditProfileScreen
import co.edu.uniquindio.truekeapp.features.progress.BadgesScreen
import co.edu.uniquindio.truekeapp.features.progress.StatisticsScreen
import co.edu.uniquindio.truekeapp.features.publication.edit.EditPublicationScreen
import co.edu.uniquindio.truekeapp.features.publication.mine.MyPublicationsScreen
import co.edu.uniquindio.truekeapp.features.notifications.NotificationsScreen
import co.edu.uniquindio.truekeapp.features.onboarding.OnboardingScreen
import co.edu.uniquindio.truekeapp.features.profile.ProfileScreen
import co.edu.uniquindio.truekeapp.features.proposals.ProposalsScreen
import co.edu.uniquindio.truekeapp.features.publication.create.CreatePublicationScreen
import co.edu.uniquindio.truekeapp.features.publication.detail.PublicationDetailScreen
import co.edu.uniquindio.truekeapp.features.publication.feed.FeedScreen
import co.edu.uniquindio.truekeapp.features.suggestions.SuggestionsScreen

private const val TRANSITION_MS = 350

@Composable
fun AppNavigation(startDestination: Any = MainRoutes.Onboarding) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
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
        // ─── Autenticación ───
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
                onLoginSuccess = {
                    navController.navigate(MainRoutes.Feed) {
                        popUpTo(MainRoutes.Onboarding) { inclusive = true }
                    }
                },
                onModeratorAccessClick = { navController.navigate(MainRoutes.ModeratorLogin) },
            )
        }

        composable<MainRoutes.ModeratorLogin> {
            ModeratorLoginScreen(
                onBackClick = { navController.popBackStack() },
                onForgotPasswordClick = { navController.navigate(MainRoutes.RecoverPassword) },
                onLoginSuccess = {
                    navController.navigate(MainRoutes.Moderation) {
                        popUpTo(MainRoutes.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        // ─── Moderador ───
        composable<MainRoutes.Moderation> {
            ModerationScreen(onLogout = { navController.logoutToOnboarding() })
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

        // ─── Pestañas principales (barra inferior) ───
        composable<MainRoutes.Feed> {
            FeedScreen(
                onPublicationClick = { navController.navigate(MainRoutes.PublicationDetail(it)) },
                onCreatePublicationClick = { navController.navigate(MainRoutes.CreatePublication) },
                onNotificationsClick = { navController.navigate(MainRoutes.Notifications) },
                onChainMatchClick = { navController.navigate(MainRoutes.ChainMatch) },
                onSuggestionsClick = { navController.navigate(MainRoutes.Suggestions) },
                onTabSelected = { navController.goToTab(it) },
            )
        }

        composable<MainRoutes.Proposals> {
            ProposalsScreen(
                onTabSelected = { navController.goToTab(it) },
                onPublicationClick = { navController.navigate(MainRoutes.PublicationDetail(it)) },
            )
        }

        composable<MainRoutes.Profile> {
            ProfileScreen(
                onTabSelected = { navController.goToTab(it) },
                onEditProfileClick = { navController.navigate(MainRoutes.EditProfile) },
                onMyPublicationsClick = { navController.navigate(MainRoutes.MyPublications) },
                onFavoritesClick = { navController.navigate(MainRoutes.Favorites) },
                onStatisticsClick = { navController.navigate(MainRoutes.Statistics) },
                onBadgesClick = { navController.navigate(MainRoutes.Badges) },
                onLogout = { navController.logoutToOnboarding() },
            )
        }

        // ─── Publicaciones ───
        composable<MainRoutes.PublicationDetail> {
            PublicationDetailScreen(
                onBackClick = { navController.popBackStack() },
                onCommentsClick = { navController.navigate(MainRoutes.Comments(it)) },
                onEditClick = { navController.navigate(MainRoutes.EditPublication(it)) },
            )
        }

        composable<MainRoutes.EditPublication> {
            EditPublicationScreen(
                onBackClick = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack()
                    // Si veníamos del detalle de esa publicación, también lo cerramos (ya no existe)
                    if (navController.currentBackStackEntry?.destination?.hasRoute<MainRoutes.PublicationDetail>() == true) {
                        navController.popBackStack()
                    }
                },
            )
        }

        composable<MainRoutes.MyPublications> {
            MyPublicationsScreen(
                onBackClick = { navController.popBackStack() },
                onPublicationClick = { navController.navigate(MainRoutes.EditPublication(it)) },
            )
        }

        // ─── Perfil ───
        composable<MainRoutes.EditProfile> {
            EditProfileScreen(
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onAccountDeleted = { navController.logoutToOnboarding() },
            )
        }

        composable<MainRoutes.Statistics> {
            StatisticsScreen(
                onBackClick = { navController.popBackStack() },
                onBadgesClick = {
                    navController.navigate(MainRoutes.Badges) {
                        popUpTo(MainRoutes.Statistics) { inclusive = true }
                    }
                },
            )
        }

        composable<MainRoutes.Badges> {
            BadgesScreen(
                onBackClick = { navController.popBackStack() },
                onStatisticsClick = {
                    navController.navigate(MainRoutes.Statistics) {
                        popUpTo(MainRoutes.Badges) { inclusive = true }
                    }
                },
            )
        }

        composable<MainRoutes.CreatePublication> {
            CreatePublicationScreen(
                onBackClick = { navController.popBackStack() },
                onPublished = { navController.popBackStack() },
            )
        }

        composable<MainRoutes.Comments> {
            CommentsScreen(onBackClick = { navController.popBackStack() })
        }

        composable<MainRoutes.Notifications> {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() },
                onNotificationClick = { navController.navigate(MainRoutes.PublicationDetail(it)) },
            )
        }

        composable<MainRoutes.Favorites> {
            FavoritesScreen(
                onBackClick = { navController.popBackStack() },
                onPublicationClick = { navController.navigate(MainRoutes.PublicationDetail(it)) },
            )
        }

        // ─── Funcionalidades innovadoras ───
        composable<MainRoutes.Suggestions> {
            SuggestionsScreen(
                onBackClick = { navController.popBackStack() },
                onPublicationClick = { navController.navigate(MainRoutes.PublicationDetail(it)) },
            )
        }

        composable<MainRoutes.ChainMatch> {
            ChainMatchScreen(onBackClick = { navController.popBackStack() })
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

// Cambia de pestaña en la barra inferior sin apilar pantallas repetidas:
// el feed siempre queda como base, así "atrás" desde Propuestas o Perfil vuelve al feed.
private fun NavHostController.goToTab(tab: MainTab) {
    when (tab) {
        MainTab.HOME -> popBackStack(MainRoutes.Feed, inclusive = false)
        MainTab.PROPOSALS -> navigate(MainRoutes.Proposals) {
            popUpTo(MainRoutes.Feed)
            launchSingleTop = true
        }
        MainTab.PROFILE -> navigate(MainRoutes.Profile) {
            popUpTo(MainRoutes.Feed)
            launchSingleTop = true
        }
    }
}

// Cierra sesión y borra toda la pila: "atrás" ya no vuelve a contenido privado
private fun NavHostController.logoutToOnboarding() {
    navigate(MainRoutes.Onboarding) {
        popUpTo(this@logoutToOnboarding.graph.id) { inclusive = true }
    }
}
