package co.edu.uniquindio.truekeapp.features.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.Suggestion
import co.edu.uniquindio.truekeapp.domain.service.SuggestionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SuggestionsUiState(
    val suggestions: List<Suggestion> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SuggestionsViewModel @Inject constructor(
    publicationRepository: PublicationRepository,
    userRepository: UserRepository,
) : ViewModel() {

    val uiState: StateFlow<SuggestionsUiState> = combine(
        publicationRepository.publications,
        userRepository.currentUser,
    ) { publications, user ->
        SuggestionsUiState(
            suggestions = user?.let { SuggestionEngine.suggest(it, publications) }.orEmpty(),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SuggestionsUiState())
}
