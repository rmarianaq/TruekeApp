package co.edu.uniquindio.truekeapp.di

import co.edu.uniquindio.truekeapp.data.repository.memory.CommentRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.NotificationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.ProposalRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.PublicationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.UserRepositoryImpl
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindPublicationRepository(impl: PublicationRepositoryImpl): PublicationRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindProposalRepository(impl: ProposalRepositoryImpl): ProposalRepository
}
