package co.edu.uniquindio.truekeapp.data.remote.openrouter

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// API REST de OpenRouter, compatible con el formato de chat de OpenAI (guía 18: interfaz de Retrofit)
// Documentación: https://openrouter.ai/docs/api-reference/chat-completion
interface OpenRouterApiService {

    @POST("api/v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequestDto,
    ): ChatResponseDto
}
