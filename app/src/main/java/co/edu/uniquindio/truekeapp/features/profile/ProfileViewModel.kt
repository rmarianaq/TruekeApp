package co.edu.uniquindio.truekeapp.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.model.UserLevel
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.TrustScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val trustScore: Int = 0,
    val publicationCount: Int = 0,
    val exchangeCount: Int = 0,
) {
    // Siguiente nivel y avance hacia él (para la barra de progreso)
    val nextLevel: UserLevel?
        get() = user?.level?.let { UserLevel.entries.getOrNull(it.ordinal + 1) }

    val levelProgress: Float
        get() {
            val u = user ?: return 0f
            val next = nextLevel ?: return 1f
            val start = u.level.minPoints
            return ((u.points - start).toFloat() / (next.minPoints - start)).coerceIn(0f, 1f)
        }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    publicationRepository: PublicationRepository,
    proposalRepository: ProposalRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userRepository.currentUser,
        publicationRepository.publications,
        proposalRepository.proposals,
    ) { user, publications, proposals ->
        if (user == null) return@combine ProfileUiState()
        ProfileUiState(
            user = user,
            trustScore = TrustScoreCalculator.calculate(user.id, publications, proposals).total,
            publicationCount = publications.count { it.ownerId == user.id },
            exchangeCount = proposals.count {
                it.status == ProposalStatus.ACCEPTED && (it.fromUserId == user.id || it.toUserId == user.id)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun logout() {
        viewModelScope.launch { userRepository.logout() }
    }
}
