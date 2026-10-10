package co.edu.uniquindio.truekeapp.data.repository.memory

import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import co.edu.uniquindio.truekeapp.domain.model.Location
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicationRepositoryImpl @Inject constructor() : PublicationRepository {

    private val _publications = MutableStateFlow(seedPublications())
    override val publications: StateFlow<List<Publication>> = _publications.asStateFlow()

    override suspend fun findById(id: String): Publication? =
        _publications.value.find { it.id == id }

    override suspend fun save(publication: Publication) {
        _publications.update { list ->
            if (list.any { it.id == publication.id }) {
                list.map { if (it.id == publication.id) publication else it }
            } else {
                listOf(publication) + list   // las nuevas van de primeras
            }
        }
    }

    override suspend fun delete(id: String) {
        _publications.update { list -> list.filterNot { it.id == id } }
    }

    // ─── Datos de prueba ─────────────────────────────────────────
    private fun seedPublications(): List<Publication> {
        val now = System.currentTimeMillis()
        val hour = 3_600_000L

        return listOf(
            Publication(
                id = "p1",
                title = "Bicicleta montañera R26",
                description = "Bicicleta en buen estado, con cambios Shimano y frenos de disco. La uso poco desde que trabajo en casa.",
                category = Category.SPORTS,
                condition = ItemCondition.GOOD,
                wantedItems = listOf("Laptop usada", "Consola de videojuegos"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-bici/800/600"),
                location = Location(4.5446, -75.6634),
                address = "Barrio La Castellana, Armenia",
                ownerId = "u2",
                status = PublicationStatus.VERIFIED,
                interestedUserIds = listOf("u1", "u3"),
                createdAt = now - 2 * hour,
            ),
            Publication(
                id = "p2",
                title = "Colección de novelas de fantasía",
                description = "Siete libros de tapa dura, como nuevos. Ideal para alguien que empieza a leer la saga.",
                category = Category.BOOKS,
                condition = ItemCondition.LIKE_NEW,
                wantedItems = listOf("Libros de ciencia ficción", "Juegos de mesa"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-libros/800/600"),
                location = Location(4.5240, -75.6920),
                address = "Centro, Armenia",
                ownerId = "u3",
                status = PublicationStatus.VERIFIED,
                interestedUserIds = listOf("u1"),
                createdAt = now - 5 * hour,
            ),
            Publication(
                id = "p3",
                title = "Audífonos inalámbricos",
                description = "Con cancelación de ruido y estuche de carga. Batería de unas 20 horas.",
                category = Category.TECHNOLOGY,
                condition = ItemCondition.GOOD,
                wantedItems = listOf("Teclado mecánico", "Parlante bluetooth"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-audifonos/800/600"),
                location = Location(4.5339, -75.6811),
                address = "Cra 14 #21-30, Armenia",
                ownerId = "u1",
                status = PublicationStatus.VERIFIED,
                createdAt = now - 9 * hour,
            ),
            Publication(
                id = "p4",
                title = "Chaqueta de jean talla M",
                description = "Usada solo dos veces, sin manchas ni rotos.",
                category = Category.CLOTHING,
                condition = ItemCondition.LIKE_NEW,
                wantedItems = listOf("Tenis talla 38", "Bolso"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-chaqueta/800/600"),
                location = Location(4.5531, -75.6589),
                address = "Barrio Granada, Armenia",
                ownerId = "u3",
                status = PublicationStatus.VERIFIED,
                interestedUserIds = listOf("u2"),
                createdAt = now - 26 * hour,
            ),
            Publication(
                id = "p5",
                title = "Cafetera de goteo",
                description = "Funciona perfecto, para 12 tazas. Incluye filtro permanente.",
                category = Category.HOME,
                condition = ItemCondition.USED,
                wantedItems = listOf("Licuadora", "Plantas"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-cafetera/800/600"),
                location = Location(4.5389, -75.6722),
                address = "Barrio El Bosque, Armenia",
                ownerId = "u2",
                status = PublicationStatus.VERIFIED,
                createdAt = now - 48 * hour,
            ),
            Publication(
                id = "p6",
                title = "Guitarra acústica",
                description = "Guitarra para principiante, con forro y afinador.",
                category = Category.OTHER,
                condition = ItemCondition.GOOD,
                wantedItems = listOf("Teclado musical"),
                imageUrls = listOf("https://picsum.photos/seed/trueke-guitarra/800/600"),
                location = Location(4.5170, -75.6850),
                address = "Barrio La Patria, Armenia",
                ownerId = "u2",
                status = PublicationStatus.PENDING,   // esta todavía no sale en el feed
                createdAt = now - 1 * hour,
            ),
        )
    }
}