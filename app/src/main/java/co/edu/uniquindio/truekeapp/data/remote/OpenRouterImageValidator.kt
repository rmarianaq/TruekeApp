package co.edu.uniquindio.truekeapp.data.remote

import android.util.Log
import co.edu.uniquindio.truekeapp.BuildConfig
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.service.ImageValidation
import co.edu.uniquindio.truekeapp.domain.service.ImageValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Funcionalidad con IA: "Validación de imágenes relevantes".
// Le pregunta a un modelo con visión (vía OpenRouter) si la foto corresponde al objeto publicado.
@Singleton
class OpenRouterImageValidator @Inject constructor(
    private val client: OkHttpClient,
) : ImageValidator {

    override suspend fun validate(imageUrl: String, title: String, category: Category): ImageValidation? =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.OPENROUTER_API_KEY
            if (apiKey.isBlank()) {
                Log.e(TAG, "Falta OPENROUTER_API_KEY en local.properties")
                return@withContext null
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

            runCatching {
                val content = JSONArray()
                    .put(JSONObject().put("type", "text").put("text", prompt))
                    .put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", imageUrl)))
                val payload = JSONObject()
                    .put("model", BuildConfig.OPENROUTER_MODEL)
                    .put("temperature", 0)
                    .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", content)))

                val request = Request.Builder()
                    .url("https://openrouter.ai/api/v1/chat/completions")
                    .header("Authorization", "Bearer $apiKey")
                    .header("X-Title", "Trueke")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val text = response.body?.string().orEmpty()
                    if (!response.isSuccessful) error("OpenRouter respondió ${response.code}: $text")
                    val answer = JSONObject(text)
                        .getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content")
                    parse(answer)
                }
            }.onFailure { Log.e(TAG, "No se pudo validar la imagen con IA", it) }.getOrNull()
        }

    // El modelo a veces envuelve el JSON en ```json ... ```; se toma lo que hay entre llaves
    private fun parse(answer: String): ImageValidation {
        val json = JSONObject(answer.substring(answer.indexOf('{'), answer.lastIndexOf('}') + 1))
        return ImageValidation(
            relevant = json.getBoolean("relevant"),
            reason = json.optString("reason"),
        )
    }

    private companion object {
        const val TAG = "OpenRouterValidator"
    }
}
