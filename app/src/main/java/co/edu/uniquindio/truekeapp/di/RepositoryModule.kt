package co.edu.uniquindio.truekeapp.di

import co.edu.uniquindio.truekeapp.data.repository.firebase.FirebaseCommentRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.firebase.FirebaseNotificationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.firebase.FirebaseProposalRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.firebase.FirebasePublicationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.firebase.FirebaseUserRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.remote.CloudinaryImageStorage
import co.edu.uniquindio.truekeapp.data.repository.remote.OpenRouterImageValidator
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.ImageStorage
import co.edu.uniquindio.truekeapp.domain.service.ImageValidator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Fase 3: los repositorios en memoria se cambian por los de Firebase.
// Las pantallas y ViewModels no cambian porque dependen de las interfaces.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FirebaseUserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPublicationRepository(impl: FirebasePublicationRepositoryImpl): PublicationRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: FirebaseCommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: FirebaseNotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindProposalRepository(impl: FirebaseProposalRepositoryImpl): ProposalRepository

    // Fase 3: servicios externos consumidos con Retrofit (guía 18)
    @Binds
    @Singleton
    abstract fun bindImageStorage(impl: CloudinaryImageStorage): ImageStorage

    @Binds
    @Singleton
    abstract fun bindImageValidator(impl: OpenRouterImageValidator): ImageValidator
}
