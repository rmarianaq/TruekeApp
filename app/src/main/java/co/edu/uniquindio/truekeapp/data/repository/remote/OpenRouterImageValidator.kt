package co.edu.uniquindio.truekeapp.data.repository.remote

import android.util.Log
import co.edu.uniquindio.truekeapp.BuildConfig
import co.edu.uniquindio.truekeapp.data.remote.openrouter.ChatMessageDto
import co.edu.uniquindio.truekeapp.data.remote.openrouter.ChatRequestDto
import co.edu.uniquindio.truekeapp.data.remote.openrouter.ContentPartDto
import co.edu.uniquindio.truekeapp.data.remote.openrouter.ImageUrlDto
import co.edu.uniquindio.truekeapp.data.remote.openrouter.ImageValidationDto
import co.edu.uniquindio.truekeapp.data.remote.openrouter.OpenRouterApiService
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.service.ImageValidation
import co.edu.uniquindio.truekeapp.domain.service.ImageValidator
import com.google.gson.Gson
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Funcionalidad con IA elegida: "Validación de imágenes relevantes".
// Le pregunta a un modelo con visión (vía OpenRouter) si la foto corresponde al objeto publicado.
@Singleton
class OpenRouterImageValidator @Inject constructor(
    private val api: OpenRouterApiService,
    private val gson: Gson,
) : ImageValidator {

    override suspend fun validate(imageUrl: String, title: String, category: Category): ImageValidation? {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        if (apiKey.isBlank()) {
            Log.e(TAG, "Falta OPENROUTER_API_KEY en local.properties")
            return null
        }
        // El motivo se pide en el idioma del teléfono para mostrarlo tal cual al usuario
        val language = if (Locale.getDefault().language == "es") "español" else "English"
        val prompt = """
            Eres el validador de imágenes de Trueke, una app de trueque de objetos.
            El usuario publicó un objeto con el título "$title" en la categoría "${category.label}".
            ¿La imagen muestra ese objeto (o uno claramente relacionado con el título y la categoría)?
            Rechaza imágenes sin relación, pantallazos, memes, contenido ofensivo o fotos sin ningún objeto.
            Responde SOLO con JSON, sin texto adicional: {"relevant": true|false, "reason": "motivo corto en $language"}
        """.trimIndent()

        val request = ChatRequestDto(
            models = (listOf(BuildConfig.OPENROUTER_MODEL) + FALLBACK_MODELS).distinct(),
            messages = listOf(
                ChatMessageDto(
                    role = "user",
                    content = listOf(
                        ContentPartDto(type = "text", text = prompt),
                        ContentPartDto(type = "image_url", imageUrl = ImageUrlDto(imageUrl)),
                    ),
                )
            ),
        )

        return runCatching {
            val answer = api.chatCompletion("Bearer $apiKey", request)
                .choices.first().message.content.orEmpty()
            // El modelo a veces envuelve el JSON en ```json ... ```; se toma lo que hay entre llaves
            val json = answer.substring(answer.indexOf('{'), answer.lastIndexOf('}') + 1)
            val dto = gson.fromJson(json, ImageValidationDto::class.java)
            ImageValidation(relevant = dto.relevant, reason = dto.reason.orEmpty())
        }.onFailure { Log.e(TAG, "No se pudo validar la imagen con IA", it) }.getOrNull()
    }

    private companion object {
        const val TAG = "OpenRouterValidator"

        // Modelos gratuitos con visión de respaldo (los :free a veces se saturan)
        val FALLBACK_MODELS = listOf(
            "google/gemma-4-31b-it:free",
            "google/gemma-4-26b-a4b-it:free",
        )
    }
}
