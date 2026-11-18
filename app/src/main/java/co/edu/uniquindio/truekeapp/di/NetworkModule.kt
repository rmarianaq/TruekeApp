package co.edu.uniquindio.truekeapp.di

import co.edu.uniquindio.truekeapp.data.remote.cloudinary.CloudinaryApiService
import co.edu.uniquindio.truekeapp.data.remote.openrouter.OpenRouterApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Clientes de las APIs REST externas (guía 18): Cloudinary (imágenes) y OpenRouter (IA)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // La IA puede tardar en responder, por eso los timeouts son amplios
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideCloudinaryApiService(client: OkHttpClient, gson: Gson): CloudinaryApiService =
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CloudinaryApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenRouterApiService(client: OkHttpClient, gson: Gson): OpenRouterApiService =
        Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OpenRouterApiService::class.java)
}
