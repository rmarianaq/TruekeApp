package co.edu.uniquindio.truekeapp.features.proposals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val POINTS_PER_EXCHANGE = 50

enum class ProposalTab { RECEIVED, SENT }

data class ProposalItem(
    val proposal: ExchangeProposal,
    val publication: Publication?,
    val otherUserName: String,   // quien la envió (recibidas) o a quien se envió (enviadas)
)

data class ProposalsUiState(
    val tab: ProposalTab = ProposalTab.RECEIVED,
    val received: List<ProposalItem> = emptyList(),
    val sent: List<ProposalItem> = emptyList(),
    val result: RequestResult? = null,
) {
    val items: List<ProposalItem> get() = if (tab == ProposalTab.RECEIVED) received else sent
}

@HiltViewModel
class ProposalsViewModel @Inject constructor(
    private val proposalRepository: ProposalRepository,
    publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(ProposalTab.RECEIVED)
    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<ProposalsUiState> = combine(
        proposalRepository.proposals,
        publicationRepository.publications,
        userRepository.users,
        userRepository.currentUser,
        combine(tab, result) { t, r -> t to r },
    ) { proposals, publications, users, currentUser, (tab, result) ->
        val pubs = publications.associateBy { it.id }
        val names = users.associate { it.id to it.name }
        val mine = proposals.sortedByDescending { it.createdAt }
        ProposalsUiState(
            tab = tab,
            received = mine
                .filter { it.toUserId == currentUser?.id }
                .map { ProposalItem(it, pubs[it.publicationId], names[it.fromUserId].orEmpty()) },
            sent = mine
                .filter { it.fromUserId == currentUser?.id }
                .map { ProposalItem(it, pubs[it.publicationId], names[it.toUserId].orEmpty()) },
            result = result,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProposalsUiState())

    fun onTabSelected(value: ProposalTab) {
        tab.value = value
    }

    fun accept(item: ProposalItem) = answer(item, ProposalStatus.ACCEPTED)

    fun reject(item: ProposalItem) = answer(item, ProposalStatus.REJECTED)

    // Responde la propuesta, avisa a quien la envió y, si se acepta, ambos ganan puntos
    private fun answer(item: ProposalItem, status: ProposalStatus) {
        val me = userRepository.currentUser.value ?: return
        viewModelScope.launch {
            proposalRepository.updateStatus(item.proposal.id, status)

            notificationRepository.add(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = item.proposal.fromUserId,
                    type = if (status == ProposalStatus.ACCEPTED) NotificationType.PROPOSAL_ACCEPTED else NotificationType.PROPOSAL_REJECTED,
                    publicationId = item.proposal.publicationId,
                    publicationTitle = item.publication?.title.orEmpty(),
                    actorName = me.name,
                )
            )

            if (status == ProposalStatus.ACCEPTED) {
                userRepository.save(me.copy(points = me.points + POINTS_PER_EXCHANGE))
                userRepository.findById(item.proposal.fromUserId)?.let { other ->
                    userRepository.save(other.copy(points = other.points + POINTS_PER_EXCHANGE))
                }
            }

            result.value = RequestResult.Success(
                if (status == ProposalStatus.ACCEPTED) R.string.proposals_accepted_msg else R.string.proposals_rejected_msg
            )
        }
    }

    fun resetResult() {
        result.value = null
    }
}
