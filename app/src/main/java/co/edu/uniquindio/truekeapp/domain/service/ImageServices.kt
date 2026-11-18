package co.edu.uniquindio.truekeapp.domain.service

import co.edu.uniquindio.truekeapp.domain.model.Category

// Contratos del dominio para las imágenes. Las implementaciones viven en data/remote
// (Cloudinary y OpenRouter), así los ViewModels no dependen de ningún proveedor.

interface ImageStorage {
    // Sube la imagen (content:// del Photo Picker) y devuelve la URL pública, o null si falla
    suspend fun upload(localUri: String): String?
}

// Resultado de preguntarle a la IA si la foto corresponde al objeto publicado
data class ImageValidation(
    val relevant: Boolean,
    val reason: String,
)

interface ImageValidator {
    // Devuelve null si la IA no respondió (sin internet, sin llave, etc.).
    // En ese caso la publicación igual queda PENDING y la revisa el moderador.
    suspend fun validate(imageUrl: String, title: String, category: Category): ImageValidation?
}
