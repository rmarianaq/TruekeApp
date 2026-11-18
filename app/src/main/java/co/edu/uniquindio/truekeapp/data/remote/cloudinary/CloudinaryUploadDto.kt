package co.edu.uniquindio.truekeapp.data.remote.cloudinary

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// Respuesta de Cloudinary al subir una imagen (solo los campos que usa la app).
// @Keep: Gson llena la clase por nombre de atributo y R8 no debe renombrarla (guía 21)
@Keep
data class CloudinaryUploadDto(
    @SerializedName("secure_url") val secureUrl: String,
    @SerializedName("public_id") val publicId: String? = null,
)
