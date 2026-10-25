package co.edu.uniquindio.truekeapp.features.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Comment
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommentItem(
    val comment: Comment,
    val authorName: String,
    val isOwner: Boolean,   // el autor es el dueño de la publicación
    val isMine: Boolean,
)

data class CommentsUiState(
    val publicationTitle: String = "",
    val comments: List<CommentItem> = emptyList(),
    val currentUserName: String = "",
    val draft: String = "",
    val result: RequestResult? = null,
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val commentRepository: CommentRepository,
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val publicationId = savedStateHandle.toRoute<MainRoutes.Comments>().publicationId
    private val draft = MutableStateFlow("")
    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<CommentsUiState> = combine(
        commentRepository.comments,
        publicationRepository.publications,
        userRepository.users,
        userRepository.currentUser,
        combine(draft, result) { d, r -> d to r },
    ) { comments, publications, users, currentUser, (draft, result) ->
        val publication = publications.find { it.id == publicationId }
        val names = users.associate { it.id to it.name }
        CommentsUiState(
            publicationTitle = publication?.title.orEmpty(),
            comments = comments
                .filter { it.publicationId == publicationId }
                .sortedBy { it.createdAt }
                .map {
                    CommentItem(
                        comment = it,
                        authorName = names[it.authorId].orEmpty(),
                        isOwner = it.authorId == publication?.ownerId,
                        isMine = it.authorId == currentUser?.id,
                    )
                },
            currentUserName = currentUser?.name.orEmpty(),
            draft = draft,
            result = result,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CommentsUiState())

    fun onDraftChange(value: String) {
        draft.value = value
    }

    // Publica el comentario y, si no lo escribió el dueño, le llega una notificación al dueño
    fun send() {
        val text = draft.value.trim()
        if (text.isEmpty()) {
            result.value = RequestResult.Failure(R.string.comments_error_empty)
            return
        }
        val me = userRepository.currentUser.value ?: return

        viewModelScope.launch {
            commentRepository.add(
                Comment(
                    id = UUID.randomUUID().toString(),
                    publicationId = publicationId,
                    authorId = me.id,
                    text = text,
                )
            )
            val publication = publicationRepository.findById(publicationId)
            if (publication != null && publication.ownerId != me.id) {
                notificationRepository.add(
                    Notification(
                        id = UUID.randomUUID().toString(),
                        userId = publication.ownerId,
                        type = NotificationType.COMMENT,
                        publicationId = publication.id,
                        publicationTitle = publication.title,
                        actorName = me.name,
                    )
                )
            }
            draft.value = ""
            result.value = RequestResult.Success(R.string.comments_success)
        }
    }

    fun resetResult() {
        result.value = null
    }
}
