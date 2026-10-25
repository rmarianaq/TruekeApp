package co.edu.uniquindio.truekeapp.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.TrustScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val POINTS_PER_VERIFIED = 20

// Las 4 pestañas del panel de moderación del mockup
enum class ModerationTab(val status: PublicationStatus) {
    PENDING(PublicationStatus.PENDING),
    VERIFIED(PublicationStatus.VERIFIED),
    FINISHED(PublicationStatus.FINISHED),
    REJECTED(PublicationStatus.REJECTED),
}

data class ModerationItem(
    val publication: Publication,
    val ownerName: String,
    val ownerTrustScore: Int,
)

data class ModerationUiState(
    val moderatorName: String = "",
    val tab: ModerationTab = ModerationTab.PENDING,
    val counts: Map<ModerationTab, Int> = emptyMap(),
    val items: List<ModerationItem> = emptyList(),
    val result: RequestResult? = null,
)

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    proposalRepository: ProposalRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(ModerationTab.PENDING)
    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<ModerationUiState> = combine(
        publicationRepository.publications,
        userRepository.users,
        proposalRepository.proposals,
        userRepository.currentUser,
        combine(tab, result) { t, r -> t to r },
    ) { publications, users, proposals, moderator, (tab, result) ->
        val names = users.associate { it.id to it.name }
        ModerationUiState(
            moderatorName = moderator?.name.orEmpty(),
            tab = tab,
            counts = ModerationTab.entries.associateWith { t -> publications.count { it.status == t.status } },
            items = publications
                .filter { it.status == tab.status }
                .sortedByDescending { it.createdAt }
                .map {
                    ModerationItem(
                        publication = it,
                        ownerName = names[it.ownerId].orEmpty(),
                        ownerTrustScore = TrustScoreCalculator.calculate(it.ownerId, publications, proposals).total,
                    )
                },
            result = result,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModerationUiState())

    fun onTabSelected(value: ModerationTab) {
        tab.value = value
    }

    // Verificar: la publicación sale al feed, el dueño gana puntos y recibe una notificación
    fun verify(publication: Publication) {
        viewModelScope.launch {
            publicationRepository.save(publication.copy(status = PublicationStatus.VERIFIED, rejectionReason = null))
            userRepository.findById(publication.ownerId)?.let {
                userRepository.save(it.copy(points = it.points + POINTS_PER_VERIFIED))
            }
            notify(publication, NotificationType.PUBLICATION_VERIFIED)
            result.value = RequestResult.Success(R.string.moderation_verified_msg)
        }
    }

    // Rechazar exige un motivo, que el usuario verá en "Mis publicaciones"
    fun reject(publication: Publication, reason: String) {
        if (reason.isBlank()) {
            result.value = RequestResult.Failure(R.string.moderation_reason_required)
            return
        }
        viewModelScope.launch {
            publicationRepository.save(publication.copy(status = PublicationStatus.REJECTED, rejectionReason = reason.trim()))
            notify(publication, NotificationType.PUBLICATION_REJECTED)
            result.value = RequestResult.Success(R.string.moderation_rejected_msg)
        }
    }

    // La guía pide que el moderador pueda marcar publicaciones como resueltas
    fun markFinished(publication: Publication) {
        viewModelScope.launch {
            publicationRepository.save(publication.copy(status = PublicationStatus.FINISHED))
            result.value = RequestResult.Success(R.string.moderation_finished_msg)
        }
    }

    fun logout() {
        viewModelScope.launch { userRepository.logout() }
    }

    fun resetResult() {
        result.value = null
    }

    private suspend fun notify(publication: Publication, type: NotificationType) {
        notificationRepository.add(
            Notification(
                id = UUID.randomUUID().toString(),
                userId = publication.ownerId,
                type = type,
                publicationId = publication.id,
                publicationTitle = publication.title,
            )
        )
    }
}
