package co.edu.uniquindio.truekeapp.data.remote.openrouter

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// DTOs de la petición y la respuesta de chat de OpenRouter.
// @Keep: Gson los llena por nombre de atributo y R8 no debe renombrarlos (guía 21)

@Keep
data class ChatRequestDto(
    // Modelos en orden de preferencia: si el primero está saturado, OpenRouter prueba el siguiente
    val models: List<String>,
    val messages: List<ChatMessageDto>,
    val temperature: Double = 0.0,
)

@Keep
data class ChatMessageDto(
    val role: String,
    val content: List<ContentPartDto>,
)

// Una parte del mensaje: texto o imagen (Gson omite los campos null)
@Keep
data class ContentPartDto(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url") val imageUrl: ImageUrlDto? = null,
)

@Keep
data class ImageUrlDto(val url: String)

@Keep
data class ChatResponseDto(val choices: List<ChoiceDto>)

@Keep
data class ChoiceDto(val message: ResponseMessageDto)

@Keep
data class ResponseMessageDto(val content: String?)

// Lo que se le pide al modelo que responda, en JSON
@Keep
data class ImageValidationDto(
    val relevant: Boolean,
    val reason: String? = null,
)
