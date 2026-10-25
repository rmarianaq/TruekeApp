package co.edu.uniquindio.truekeapp.features.publication.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.repository.CommentRepository
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.GeoUtils
import co.edu.uniquindio.truekeapp.domain.service.TrustScoreCalculator
import co.edu.uniquindio.truekeapp.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PublicationDetailUiState(
    val isLoading: Boolean = true,
    val publication: Publication? = null,
    val owner: User? = null,
    val ownerTrustScore: Int = 0,
    val currentUserId: String? = null,
    val commentCount: Int = 0,
    val distanceKm: Double? = null,
    val hasPendingProposal: Boolean = false,
    val result: RequestResult? = null,
) {
    val isMine: Boolean
        get() = publication != null && publication.ownerId == currentUserId

    val isInterested: Boolean
        get() = publication != null && currentUserId != null && currentUserId in publication.interestedUserIds

    val isReportedByMe: Boolean
        get() = publication != null && currentUserId != null && currentUserId in publication.reportedByUserIds
}

@HiltViewModel
class PublicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    commentRepository: CommentRepository,
    private val proposalRepository: ProposalRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    // El id llega desde la ruta: MainRoutes.PublicationDetail(publicationId = "p1")
    private val publicationId: String =
        savedStateHandle.toRoute<MainRoutes.PublicationDetail>().publicationId

    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<PublicationDetailUiState> = combine(
        publicationRepository.publications,
        userRepository.currentUser,
        commentRepository.comments,
        proposalRepository.proposals,
        result,
    ) { publications, currentUser, comments, proposals, result ->
        val publication = publications.find { it.id == publicationId }
        val owner = publication?.let { userRepository.findById(it.ownerId) }
        PublicationDetailUiState(
            isLoading = false,
            publication = publication,
            owner = owner,
            ownerTrustScore = owner?.let { TrustScoreCalculator.calculate(it.id, publications, proposals).total } ?: 0,
            currentUserId = currentUser?.id,
            commentCount = comments.count { it.publicationId == publicationId },
            distanceKm = currentUser?.location?.let { loc -> publication?.let { GeoUtils.distanceKm(loc, it.location) } },
            hasPendingProposal = proposals.any {
                it.publicationId == publicationId && it.fromUserId == currentUser?.id && it.status == ProposalStatus.PENDING
            },
            result = result,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PublicationDetailUiState(),
    )

    fun toggleInterest() {
        val userId = userRepository.currentUser.value?.id ?: return
        val wasInterested = uiState.value.isInterested

        viewModelScope.launch {
            publicationRepository.toggleInterest(publicationId, userId)
            result.value = RequestResult.Success(
                if (wasInterested) R.string.detail_interest_removed else R.string.detail_interest_added
            )
        }
    }

    fun report() {
        val userId = userRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            publicationRepository.report(publicationId, userId)
            result.value = RequestResult.Success(R.string.detail_report_success)
        }
    }

    // Crea la propuesta de trueque y le avisa al dueño con una notificación
    fun propose(offeredItem: String) {
        val me = userRepository.currentUser.value ?: return
        val publication = uiState.value.publication ?: return
        if (offeredItem.isBlank()) {
            result.value = RequestResult.Failure(R.string.propose_error_empty)
            return
        }

        viewModelScope.launch {
            result.value = RequestResult.Loading
            delay(800)
            proposalRepository.save(
                ExchangeProposal(
                    id = UUID.randomUUID().toString(),
                    publicationId = publication.id,
                    fromUserId = me.id,
                    toUserId = publication.ownerId,
                    offeredItem = offeredItem.trim(),
                )
            )
            notificationRepository.add(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = publication.ownerId,
                    type = NotificationType.PROPOSAL_RECEIVED,
                    publicationId = publication.id,
                    publicationTitle = publication.title,
                    actorName = me.name,
                )
            )
            result.value = RequestResult.Success(R.string.propose_success)
        }
    }

    fun resetResult() {
        result.value = null
    }
}
