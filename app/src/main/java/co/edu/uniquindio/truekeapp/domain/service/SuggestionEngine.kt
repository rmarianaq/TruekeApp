package co.edu.uniquindio.truekeapp.domain.service

import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.model.User

enum class SuggestionReason {
    CATEGORY_INTEREST,   // es de una categoría que te interesa
    POPULAR,             // muchas personas la quieren
    NEARBY,              // está cerca de ti
    NEW,                 // novedad
}

data class Suggestion(
    val publication: Publication,
    val reason: SuggestionReason,
    val distanceKm: Double?,
)

// Funcionalidad innovadora 3: Rutas de trueque sugeridas.
// Puntúa cada publicación combinando tus categorías favoritas (según tus "Me interesa"),
// la popularidad y la cercanía, y devuelve las mejores con el motivo de la recomendación.
object SuggestionEngine {

    fun suggest(user: User, publications: List<Publication>, limit: Int = 10): List<Suggestion> {
        val favoriteCategories = publications
            .filter { user.id in it.interestedUserIds }
            .map { it.category }
            .toSet()

        return publications
            .filter {
                it.status == PublicationStatus.VERIFIED &&
                    it.ownerId != user.id &&
                    user.id !in it.interestedUserIds
            }
            .map { pub ->
                val distance = user.location?.let { GeoUtils.distanceKm(it, pub.location) }
                val categoryScore = if (pub.category in favoriteCategories) 3.0 else 0.0
                val popularScore = if (pub.interestCount >= 2) 2.0 else 0.0
                val nearbyScore = if (distance != null && distance <= 3.0) 1.5 else 0.0

                val reason = when {
                    categoryScore > 0 -> SuggestionReason.CATEGORY_INTEREST
                    popularScore > 0 -> SuggestionReason.POPULAR
                    nearbyScore > 0 -> SuggestionReason.NEARBY
                    else -> SuggestionReason.NEW
                }
                Triple(Suggestion(pub, reason, distance), categoryScore + popularScore + nearbyScore, pub.createdAt)
            }
            .sortedWith(compareByDescending<Triple<Suggestion, Double, Long>> { it.second }.thenByDescending { it.third })
            .take(limit)
            .map { it.first }
    }
}
