package co.edu.uniquindio.truekeapp.data.repository.firebase

import android.util.Log
import co.edu.uniquindio.truekeapp.data.firebase.toComment
import co.edu.uniquindio.truekeapp.data.firebase.toMap
import co.edu.uniquindio.truekeapp.data.firebase.toNotification
import co.edu.uniquindio.truekeapp.data.firebase.toProposal
import co.edu.uniquindio.truekeapp.domain.model.Comment
import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// Repositorios de comentarios, notificaciones y propuestas en Firestore.
// Igual que en la guía: un listener mantiene la lista al día y las escrituras usan await().

@Singleton
class FirebaseCommentRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
) : CommentRepository {

    private val collection = firestore.collection("comments")
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    override val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    init {
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error escuchando comments", error)
                return@addSnapshotListener
            }
            _comments.value = snapshot?.documents?.mapNotNull { it.toComment() } ?: return@addSnapshotListener
        }
    }

    override suspend fun add(comment: Comment) {
        collection.document(comment.id).set(comment.toMap()).await()
    }
}

@Singleton
class FirebaseNotificationRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
) : NotificationRepository {

    private val collection = firestore.collection("notifications")
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    override val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error escuchando notifications", error)
                return@addSnapshotListener
            }
            _notifications.value = snapshot?.documents?.mapNotNull { it.toNotification() }
                ?.sortedByDescending { it.createdAt } ?: return@addSnapshotListener
        }
    }

    override suspend fun add(notification: Notification) {
        collection.document(notification.id).set(notification.toMap()).await()
    }

    override suspend fun setRead(id: String, isRead: Boolean) {
        collection.document(id).update("isRead", isRead).await()
    }

    override suspend fun markAllAsRead(userId: String) {
        // Un batch aplica todos los cambios en una sola escritura
        val unread = collection.whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false).get().await().documents
        if (unread.isEmpty()) return
        val batch = collection.firestore.batch()
        unread.forEach { batch.update(it.reference, "isRead", true) }
        batch.commit().await()
    }
}

@Singleton
class FirebaseProposalRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
) : ProposalRepository {

    private val collection = firestore.collection("proposals")
    private val _proposals = MutableStateFlow<List<ExchangeProposal>>(emptyList())
    override val proposals: StateFlow<List<ExchangeProposal>> = _proposals.asStateFlow()

    init {
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error escuchando proposals", error)
                return@addSnapshotListener
            }
            _proposals.value = snapshot?.documents?.mapNotNull { it.toProposal() }
                ?.sortedByDescending { it.createdAt } ?: return@addSnapshotListener
        }
    }

    override suspend fun save(proposal: ExchangeProposal) {
        collection.document(proposal.id).set(proposal.toMap()).await()
    }

    override suspend fun updateStatus(id: String, status: ProposalStatus) {
        collection.document(id).update("status", status.name).await()
    }
}

private const val TAG = "FirestoreRepository"
