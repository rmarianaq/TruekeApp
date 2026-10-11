package co.edu.uniquindio.truekeapp.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {
    @Serializable data object Onboarding : MainRoutes()
    @Serializable data object Login : MainRoutes()
    @Serializable data object Register : MainRoutes()
    @Serializable data object RecoverPassword : MainRoutes()
    @Serializable data object Feed : MainRoutes()
    @Serializable data class PublicationDetail(val publicationId: String) : MainRoutes()
    @Serializable data object CreatePublication : MainRoutes()
}