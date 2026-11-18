package co.edu.uniquindio.truekeapp.di

import co.edu.uniquindio.truekeapp.data.remote.CloudinaryImageStorage
import co.edu.uniquindio.truekeapp.data.remote.OpenRouterImageValidator
import co.edu.uniquindio.truekeapp.domain.service.ImageStorage
import co.edu.uniquindio.truekeapp.domain.service.ImageValidator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Servicios externos de la Fase 3: Cloudinary (imágenes) y OpenRouter (IA)
@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    @Singleton
    abstract fun bindImageStorage(impl: CloudinaryImageStorage): ImageStorage

    @Binds
    @Singleton
    abstract fun bindImageValidator(impl: OpenRouterImageValidator): ImageValidator

    companion object {
        // Un solo cliente HTTP para toda la app; la IA puede tardar, por eso el timeout es amplio
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
