package co.edu.uniquindio.truekeapp.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.GeoUtils
import co.edu.uniquindio.truekeapp.features.publication.feed.FeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // Las publicaciones donde el usuario marcó "Me interesa"
    val uiState: StateFlow<FavoritesUiState> = combine(
        publicationRepository.publications,
        userRepository.users,
        userRepository.currentUser,
    ) { publications, users, currentUser ->
        val names = users.associate { it.id to it.name }
        FavoritesUiState(
            items = publications
                .filter { currentUser != null && currentUser.id in it.interestedUserIds }
                .map {
                    FeedItem(
                        publication = it,
                        ownerName = names[it.ownerId].orEmpty(),
                        isMine = false,
                        isInterested = true,
                        distanceKm = currentUser?.location?.let { loc -> GeoUtils.distanceKm(loc, it.location) },
                    )
                },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    fun toggleInterest(publicationId: String) {
        val userId = userRepository.currentUser.value?.id ?: return
        viewModelScope.launch { publicationRepository.toggleInterest(publicationId, userId) }
    }
}
