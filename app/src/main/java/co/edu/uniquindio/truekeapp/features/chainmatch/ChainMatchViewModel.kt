package co.edu.uniquindio.truekeapp.features.chainmatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.ExchangeProposal
import co.edu.uniquindio.truekeapp.domain.model.Notification
import co.edu.uniquindio.truekeapp.domain.model.NotificationType
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.repository.NotificationRepository
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.ChainMatch
import co.edu.uniquindio.truekeapp.domain.service.ChainMatchFinder
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

data class ChainMatchUiState(
    val chainMatch: ChainMatch? = null,
    val users: Map<String, User> = emptyMap(),
    val currentUserId: String? = null,
    val alreadyProposed: Boolean = false,
    val result: RequestResult? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class ChainMatchViewModel @Inject constructor(
    publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val proposalRepository: ProposalRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<ChainMatchUiState> = combine(
        publicationRepository.publications,
        userRepository.users,
        userRepository.currentUser,
        proposalRepository.proposals,
        result,
    ) { publications, users, currentUser, proposals, result ->
        val chain = currentUser?.let { ChainMatchFinder.find(it.id, publications) }
        val chainPublicationIds = chain?.steps?.map { it.publication.id }.orEmpty()
        ChainMatchUiState(
            chainMatch = chain,
            users = users.associateBy { it.id },
            currentUserId = currentUser?.id,
            alreadyProposed = proposals.any {
                it.isChain && it.fromUserId == currentUser?.id && it.publicationId in chainPublicationIds
            },
            result = result,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChainMatchUiState())

    // Envía una propuesta a cada participante del ciclo y le notifica
    fun proposeChain() {
        val state = uiState.value
        val chain = state.chainMatch ?: return
        val me = userRepository.currentUser.value ?: return

        viewModelScope.launch {
            result.value = RequestResult.Loading
            delay(800)
            chain.steps
                .filter { it.giverId != me.id }
                .forEach { step ->
                    // Lo que esa persona recibe en el ciclo
                    val receives = chain.steps.firstOrNull { it.receiverId == step.giverId }?.publication?.title.orEmpty()
                    proposalRepository.save(
                        ExchangeProposal(
                            id = UUID.randomUUID().toString(),
                            publicationId = step.publication.id,
                            fromUserId = me.id,
                            toUserId = step.giverId,
                            offeredItem = receives,
                            isChain = true,
                        )
                    )
                    notificationRepository.add(
                        Notification(
                            id = UUID.randomUUID().toString(),
                            userId = step.giverId,
                            type = NotificationType.PROPOSAL_RECEIVED,
                            publicationId = step.publication.id,
                            publicationTitle = step.publication.title,
                            actorName = me.name,
                        )
                    )
                }
            result.value = RequestResult.Success(R.string.chain_proposed)
        }
    }

    fun resetResult() {
        result.value = null
    }
}
