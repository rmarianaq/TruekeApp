package co.edu.uniquindio.truekeapp.domain.repository

import co.edu.uniquindio.truekeapp.domain.model.Publication
import kotlinx.coroutines.flow.StateFlow

interface PublicationRepository {

    // Todas las publicaciones; la pantalla se actualiza sola cuando cambian
    val publications: StateFlow<List<Publication>>

    suspend fun findById(id: String): Publication?
    suspend fun save(publication: Publication)
    suspend fun delete(id: String)
    suspend fun toggleInterest(publicationId: String, userId: String)
}