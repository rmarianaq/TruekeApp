package co.edu.uniquindio.truekeapp.features.progress

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ProposalStatus
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.model.UserLevel
import co.edu.uniquindio.truekeapp.domain.repository.ProposalRepository
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.TrustScore
import co.edu.uniquindio.truekeapp.domain.service.TrustScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class BadgeItem(
    @StringRes val nameRes: Int,
    @StringRes val hintRes: Int,
    val unlocked: Boolean,
)

data class ProgressUiState(
    val user: User? = null,
    val active: Int = 0,
    val finished: Int = 0,
    val pending: Int = 0,
    val byCategory: List<Pair<Category, Int>> = emptyList(),
    val trust: TrustScore = TrustScore(100, 100, 100),
    val badges: List<BadgeItem> = emptyList(),
) {
    val nextLevel: UserLevel? get() = user?.level?.let { UserLevel.entries.getOrNull(it.ordinal + 1) }
    val unlockedBadges: Int get() = badges.count { it.unlocked }
}

// Lo comparten Estadísticas e Insignias (el dashboard de gamificación que pide la guía)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    publicationRepository: PublicationRepository,
    proposalRepository: ProposalRepository,
    userRepository: UserRepository,
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        publicationRepository.publications,
        proposalRepository.proposals,
        userRepository.currentUser,
    ) { publications, proposals, user ->
        if (user == null) return@combine ProgressUiState()

        val mine = publications.filter { it.ownerId == user.id }
        val trust = TrustScoreCalculator.calculate(user.id, publications, proposals)
        val exchanges = proposals.count {
            it.status == ProposalStatus.ACCEPTED && (it.fromUserId == user.id || it.toUserId == user.id)
        }
        val verifiedCount = mine.count { it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.FINISHED }

        ProgressUiState(
            user = user,
            active = mine.count { it.status == PublicationStatus.VERIFIED },
            finished = mine.count { it.status == PublicationStatus.FINISHED },
            pending = mine.count { it.status == PublicationStatus.PENDING },
            byCategory = mine.groupingBy { it.category }.eachCount().toList().sortedByDescending { it.second },
            trust = trust,
            badges = listOf(
                BadgeItem(R.string.badge_first, R.string.badge_first_hint, mine.isNotEmpty()),
                BadgeItem(R.string.badge_ten_verified, R.string.badge_ten_verified_hint, verifiedCount >= 10),
                BadgeItem(R.string.badge_featured, R.string.badge_featured_hint, mine.any { it.interestCount >= 3 }),
                BadgeItem(R.string.badge_five_exchanges, R.string.badge_five_exchanges_hint, exchanges >= 5),
                BadgeItem(R.string.badge_trusted, R.string.badge_trusted_hint, trust.total >= 90),
                BadgeItem(R.string.badge_master, R.string.badge_master_hint, user.level == UserLevel.MAESTRO_DEL_TRUEQUE),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())
}
