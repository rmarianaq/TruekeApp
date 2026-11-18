package co.edu.uniquindio.truekeapp.data.remote

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import co.edu.uniquindio.truekeapp.BuildConfig
import co.edu.uniquindio.truekeapp.domain.service.ImageStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

// Sube las fotos a Cloudinary con un "upload preset" sin firma (unsigned).
// Se usa Cloudinary porque Firebase Storage exige el plan Blaze (de pago).
@Singleton
class CloudinaryImageStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val client: OkHttpClient,
) : ImageStorage {

    override suspend fun upload(localUri: String): String? = withContext(Dispatchers.IO) {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        if (cloudName.isBlank()) {
            Log.e(TAG, "Falta CLOUDINARY_CLOUD_NAME en local.properties")
            return@withContext null
        }
        runCatching {
            val uri = localUri.toUri()
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "trueke.jpg", bytes.toRequestBody(mime.toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder", "trueke")
                .build()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("Cloudinary respondió ${response.code}: $text")
                JSONObject(text).getString("secure_url")
            }
        }.onFailure { Log.e(TAG, "No se pudo subir la imagen", it) }.getOrNull()
    }

    private companion object {
        const val TAG = "CloudinaryImageStorage"
    }
}
