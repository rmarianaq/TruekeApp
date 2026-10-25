package co.edu.uniquindio.truekeapp.features.publication.edit

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Qué pasó después de una acción, para que la pantalla sepa si debe cerrarse
enum class EditAction { SAVED, DELETED, STATUS_CHANGED }

data class EditPublicationUiState(
    val original: Publication? = null,
    val title: String = "",
    val description: String = "",
    val category: Category = Category.OTHER,
    val condition: ItemCondition = ItemCondition.GOOD,
    val wantedItems: String = "",
    val address: String = "",
    val status: PublicationStatus = PublicationStatus.PENDING,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val wantedError: Int? = null,
    @StringRes val addressError: Int? = null,
    val lastAction: EditAction? = null,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
    val isFinished: Boolean get() = status == PublicationStatus.FINISHED
    // Solo se puede finalizar un trueque que ya fue verificado
    val canToggleFinished: Boolean get() = status == PublicationStatus.VERIFIED || status == PublicationStatus.FINISHED
}

@HiltViewModel
class EditPublicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationRepository: PublicationRepository,
) : ViewModel() {

    private val publicationId = savedStateHandle.toRoute<MainRoutes.EditPublication>().publicationId

    private val _uiState = MutableStateFlow(EditPublicationUiState())
    val uiState: StateFlow<EditPublicationUiState> = _uiState.asStateFlow()

    init {
        // Carga los datos actuales para precargar el formulario
        viewModelScope.launch {
            publicationRepository.findById(publicationId)?.let { p ->
                _uiState.update {
                    it.copy(
                        original = p,
                        title = p.title,
                        description = p.description,
                        category = p.category,
                        condition = p.condition,
                        wantedItems = p.wantedItems.joinToString(", "),
                        address = p.address,
                        status = p.status,
                    )
                }
            }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v, descriptionError = null) }
    fun onCategoryChange(v: Category) = _uiState.update { it.copy(category = v) }
    fun onConditionChange(v: ItemCondition) = _uiState.update { it.copy(condition = v) }
    fun onWantedChange(v: String) = _uiState.update { it.copy(wantedItems = v, wantedError = null) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, addressError = null) }

    fun save() {
        val s = _uiState.value
        val original = s.original ?: return
        val wanted = s.wantedItems.split(",").map { it.trim() }.filter { it.isNotBlank() }

        val titleError = if (s.title.trim().length < 5) R.string.error_title_required else null
        val descriptionError = if (s.description.trim().length < 15) R.string.error_description_required else null
        val wantedError = if (wanted.isEmpty()) R.string.error_wanted_required else null
        val addressError = if (s.address.isBlank()) R.string.error_address_required else null

        if (listOf(titleError, descriptionError, wantedError, addressError).any { it != null }) {
            _uiState.update {
                it.copy(titleError = titleError, descriptionError = descriptionError, wantedError = wantedError, addressError = addressError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            delay(800)
            publicationRepository.save(
                original.copy(
                    title = s.title.trim(),
                    description = s.description.trim(),
                    category = s.category,
                    condition = s.condition,
                    wantedItems = wanted,
                    address = s.address.trim(),
                    status = s.status,
                )
            )
            _uiState.update { it.copy(lastAction = EditAction.SAVED, result = RequestResult.Success(R.string.edit_saved)) }
        }
    }

    // "Marcar trueque como finalizado" / "Reabrir", como en el mockup
    fun toggleFinished() {
        val s = _uiState.value
        val original = s.original ?: return
        if (!s.canToggleFinished) return
        val newStatus = if (s.isFinished) PublicationStatus.VERIFIED else PublicationStatus.FINISHED
        viewModelScope.launch {
            publicationRepository.save(original.copy(status = newStatus))
            _uiState.update {
                it.copy(
                    status = newStatus,
                    original = original.copy(status = newStatus),
                    lastAction = EditAction.STATUS_CHANGED,
                    result = RequestResult.Success(if (newStatus == PublicationStatus.FINISHED) R.string.edit_finished else R.string.edit_reopened),
                )
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            publicationRepository.delete(publicationId)
            _uiState.update { it.copy(lastAction = EditAction.DELETED, result = RequestResult.Success(R.string.edit_deleted)) }
        }
    }

    fun resetResult() = _uiState.update { it.copy(result = null) }
}
