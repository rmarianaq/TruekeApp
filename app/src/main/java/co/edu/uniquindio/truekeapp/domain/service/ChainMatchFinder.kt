package co.edu.uniquindio.truekeapp.domain.service

import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus

// Un paso del ciclo: "giver" entrega "publication" y la recibe "receiver"
data class ChainStep(
    val giverId: String,
    val publication: Publication,
    val receiverId: String,
)

data class ChainMatch(val steps: List<ChainStep>) {
    val participantIds: List<String> get() = steps.map { it.giverId }
}

// Funcionalidad innovadora 1: Matching en cadena.
// Armamos un GRAFO DIRIGIDO de intereses: hay una flecha A -> B cuando A marcó
// "Me interesa" en una publicación de B. Si encontramos un CICLO que vuelve al
// usuario (A -> B -> C -> A), los tres pueden intercambiar sin usar dinero.
object ChainMatchFinder {

    fun find(userId: String, publications: List<Publication>, maxLength: Int = 4): ChainMatch? {
        val active = publications.filter { it.status == PublicationStatus.VERIFIED }

        // wants[A] = publicaciones (de otros) que A quiere
        val wants: Map<String, List<Publication>> = active
            .flatMap { pub ->
                pub.interestedUserIds
                    .filter { it != pub.ownerId }
                    .map { interested -> interested to pub }
            }
            .groupBy({ it.first }, { it.second })

        // Búsqueda en profundidad (DFS) de un ciclo de 3 o más personas
        fun search(path: List<String>, items: List<Publication>): Pair<List<String>, List<Publication>>? {
            val current = path.last()
            for (pub in wants[current].orEmpty()) {
                val owner = pub.ownerId
                if (owner == userId && path.size >= 3) return path to (items + pub)
                if (owner !in path && path.size < maxLength) {
                    search(path + owner, items + pub)?.let { return it }
                }
            }
            return null
        }

        val (path, items) = search(listOf(userId), emptyList()) ?: return null

        // items[i] es la publicación que path[i] quiere (y pertenece al siguiente del ciclo).
        // Lo recorremos al revés para que el primer paso sea "Tú entregas...".
        val steps = items.indices.reversed().map { i ->
            ChainStep(
                giverId = items[i].ownerId,
                publication = items[i],
                receiverId = path[i],
            )
        }
        return ChainMatch(steps)
    }
}
