package co.edu.uniquindio.truekeapp.features.publication.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.User
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
import javax.inject.Inject

data class PublicationDetailUiState(
    val isLoading: Boolean = true,
    val publication: Publication? = null,
    val owner: User? = null,
    val currentUserId: String? = null,
    val result: RequestResult? = null,
) {
    val isMine: Boolean
        get() = publication != null && publication.ownerId == currentUserId

    val isInterested: Boolean
        get() = publication != null && currentUserId != null && currentUserId in publication.interestedUserIds
}

@HiltViewModel
class PublicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // El id llega desde la ruta: MainRoutes.PublicationDetail(publicationId = "p1")
    private val publicationId: String =
        savedStateHandle.toRoute<MainRoutes.PublicationDetail>().publicationId

    private val result = MutableStateFlow<RequestResult?>(null)

    val uiState: StateFlow<PublicationDetailUiState> = combine(
        publicationRepository.publications,
        userRepository.currentUser,
        result,
    ) { publications, currentUser, result ->
        val publication = publications.find { it.id == publicationId }
        PublicationDetailUiState(
            isLoading = false,
            publication = publication,
            owner = publication?.let { userRepository.findById(it.ownerId) },
            currentUserId = currentUser?.id,
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

    fun resetResult() {
        result.value = null
    }
}