package co.edu.uniquindio.truekeapp.features.publication.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MyPublicationsUiState(
    val publications: List<Publication> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class MyPublicationsViewModel @Inject constructor(
    publicationRepository: PublicationRepository,
    userRepository: UserRepository,
) : ViewModel() {

    val uiState: StateFlow<MyPublicationsUiState> = combine(
        publicationRepository.publications,
        userRepository.currentUser,
    ) { publications, user ->
        MyPublicationsUiState(
            publications = publications.filter { it.ownerId == user?.id }.sortedByDescending { it.createdAt },
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyPublicationsUiState())
}
