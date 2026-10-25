package co.edu.uniquindio.truekeapp.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {
    // Autenticación
    @Serializable data object Onboarding : MainRoutes()
    @Serializable data object Login : MainRoutes()
    @Serializable data object Register : MainRoutes()
    @Serializable data object RecoverPassword : MainRoutes()
    @Serializable data object ModeratorLogin : MainRoutes()

    // Usuario
    @Serializable data object Feed : MainRoutes()
    @Serializable data class PublicationDetail(val publicationId: String) : MainRoutes()
    @Serializable data object CreatePublication : MainRoutes()
    @Serializable data class EditPublication(val publicationId: String) : MainRoutes()
    @Serializable data class Comments(val publicationId: String) : MainRoutes()
    @Serializable data object Notifications : MainRoutes()
    @Serializable data object Proposals : MainRoutes()
    @Serializable data object Profile : MainRoutes()
    @Serializable data object EditProfile : MainRoutes()
    @Serializable data object MyPublications : MainRoutes()
    @Serializable data object Favorites : MainRoutes()
    @Serializable data object Statistics : MainRoutes()
    @Serializable data object Badges : MainRoutes()

    // Funcionalidades innovadoras
    @Serializable data object Suggestions : MainRoutes()
    @Serializable data object ChainMatch : MainRoutes()

    // Moderador
    @Serializable data object Moderation : MainRoutes()
}
