package co.edu.uniquindio.truekeapp.features.publication.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Lo que necesita la tarjeta: la publicación + datos ya "masticados" para mostrar
data class FeedItem(
    val publication: Publication,
    val ownerName: String,
    val isMine: Boolean,
    val isInterested: Boolean,
)

data class FeedUiState(
    val userName: String = "",
    val query: String = "",
    val selectedCategory: Category? = null,
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<Category?>(null)

    // El estado se CALCULA combinando 4 fuentes. Si cualquiera cambia, se recalcula solo.
    val uiState: StateFlow<FeedUiState> = combine(
        publicationRepository.publications,
        userRepository.currentUser,
        query,
        selectedCategory,
    ) { publications, currentUser, query, category ->

        val items = publications
            .filter { it.status == PublicationStatus.VERIFIED }
            .filter { category == null || it.category == category }
            .filter {
                query.isBlank() ||
                        it.title.contains(query.trim(), ignoreCase = true) ||
                        it.description.contains(query.trim(), ignoreCase = true)
            }
            .sortedByDescending { it.createdAt }
            .map { publication ->
                FeedItem(
                    publication = publication,
                    ownerName = userRepository.findById(publication.ownerId)?.name.orEmpty(),
                    isMine = publication.ownerId == currentUser?.id,
                    isInterested = currentUser != null && currentUser.id in publication.interestedUserIds,
                )
            }

        FeedUiState(
            userName = currentUser?.name?.substringBefore(" ").orEmpty(),
            query = query,
            selectedCategory = category,
            items = items,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    // Tocar la categoría ya seleccionada la quita (vuelve a "Todas")
    fun onCategorySelected(category: Category?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun toggleInterest(publicationId: String) {
        val userId = userRepository.currentUser.value?.id ?: return
        viewModelScope.launch {
            publicationRepository.toggleInterest(publicationId, userId)
        }
    }

    fun logout() {
        viewModelScope.launch { userRepository.logout() }
    }
}