package co.edu.uniquindio.truekeapp.data.repository.firebase

import android.util.Log
import co.edu.uniquindio.truekeapp.data.firebase.toMap
import co.edu.uniquindio.truekeapp.data.firebase.toPublication
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePublicationRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
) : PublicationRepository {

    private val collection = firestore.collection("publications")

    private val _publications = MutableStateFlow<List<Publication>>(emptyList())
    override val publications: StateFlow<List<Publication>> = _publications.asStateFlow()

    init {
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("PublicationRepository", "Error escuchando publicaciones", error)
                return@addSnapshotListener
            }
            _publications.value = snapshot?.documents
                ?.mapNotNull { it.toPublication() }
                ?.sortedByDescending { it.createdAt }
                ?: return@addSnapshotListener
        }
    }

    override suspend fun findById(id: String): Publication? =
        _publications.value.find { it.id == id } ?: collection.document(id).get().await().toPublication()

    override suspend fun save(publication: Publication) {
        collection.document(publication.id).set(publication.toMap()).await()
    }

    override suspend fun delete(id: String) {
        collection.document(id).delete().await()
    }

    override suspend fun deleteByOwner(ownerId: String) {
        val docs = collection.whereEqualTo("ownerId", ownerId).get().await().documents
        docs.forEach { it.reference.delete().await() }
    }

    override suspend fun toggleInterest(publicationId: String, userId: String) {
        val current = findById(publicationId) ?: return
        // arrayUnion / arrayRemove cambian solo ese campo, sin pisar otros cambios
        val change = if (userId in current.interestedUserIds) {
            FieldValue.arrayRemove(userId)
        } else {
            FieldValue.arrayUnion(userId)
        }
        collection.document(publicationId).update("interestedUserIds", change).await()
    }

    override suspend fun report(publicationId: String, userId: String) {
        collection.document(publicationId)
            .update("reportedByUserIds", FieldValue.arrayUnion(userId)).await()
    }
}
