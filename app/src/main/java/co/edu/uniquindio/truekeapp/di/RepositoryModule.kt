package co.edu.uniquindio.truekeapp.di

import co.edu.uniquindio.truekeapp.data.repository.memory.PublicationRepositoryImpl
import co.edu.uniquindio.truekeapp.data.repository.memory.UserRepositoryImpl
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
}