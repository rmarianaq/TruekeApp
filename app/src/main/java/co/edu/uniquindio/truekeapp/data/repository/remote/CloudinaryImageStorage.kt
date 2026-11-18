package co.edu.uniquindio.truekeapp.data.repository.remote

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import co.edu.uniquindio.truekeapp.BuildConfig
import co.edu.uniquindio.truekeapp.data.remote.cloudinary.CloudinaryApiService
import co.edu.uniquindio.truekeapp.domain.service.ImageStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

// Sube las fotos a Cloudinary con un "upload preset" sin firma (unsigned).
// Se usa Cloudinary porque Firebase Storage exige el plan Blaze (de pago).
@Singleton
class CloudinaryImageStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: CloudinaryApiService,
) : ImageStorage {

    override suspend fun upload(localUri: String): String? {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        if (cloudName.isBlank()) {
            Log.e(TAG, "Falta CLOUDINARY_CLOUD_NAME en local.properties")
            return null
        }
        return runCatching {
            // Lee los bytes de la foto elegida en el Photo Picker (content://)
            val uri = localUri.toUri()
            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
            }
            val file = MultipartBody.Part.createFormData(
                "file", "trueke.jpg", bytes.toRequestBody(mime.toMediaTypeOrNull())
            )
            val text = "text/plain".toMediaTypeOrNull()
            api.uploadImage(
                cloudName = cloudName,
                file = file,
                uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.toRequestBody(text),
                folder = "trueke".toRequestBody(text),
            ).secureUrl
        }.onFailure { Log.e(TAG, "No se pudo subir la imagen", it) }.getOrNull()
    }

    private companion object {
        const val TAG = "CloudinaryImageStorage"
    }
}
