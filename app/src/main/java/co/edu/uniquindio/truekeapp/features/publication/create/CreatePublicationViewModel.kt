package co.edu.uniquindio.truekeapp.features.publication.create

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import co.edu.uniquindio.truekeapp.domain.model.Location
import co.edu.uniquindio.truekeapp.domain.model.Publication
import co.edu.uniquindio.truekeapp.domain.model.PublicationStatus
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import co.edu.uniquindio.truekeapp.domain.service.ImageStorage
import co.edu.uniquindio.truekeapp.domain.service.ImageValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreatePublicationUiState(
    val title: String = "",
    val description: String = "",
    val category: Category? = null,
    val condition: ItemCondition = ItemCondition.GOOD,
    val wantedItems: String = "",
    val address: String = "",
    // Foto elegida con el Photo Picker (content://). Se sube a Cloudinary al publicar
    val imageUri: String? = null,
    // Punto de intercambio elegido en el mapa (si no se elige, se usa la ubicación del usuario)
    val location: Location? = null,
    @StringRes val imageError: Int? = null,
    @StringRes val titleError: Int? = null,
    @StringRes val descriptionError: Int? = null,
    @StringRes val categoryError: Int? = null,
    @StringRes val wantedError: Int? = null,
    @StringRes val addressError: Int? = null,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
}

@HiltViewModel
class CreatePublicationViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository,
    private val imageStorage: ImageStorage,
    private val imageValidator: ImageValidator,
) : ViewModel() {

    // Centro inicial del mapa: la ubicación registrada del usuario
    val userLocation: Location? get() = userRepository.currentUser.value?.location

    private val _uiState = MutableStateFlow(CreatePublicationUiState())
    val uiState: StateFlow<CreatePublicationUiState> = _uiState.asStateFlow()

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v, descriptionError = null) }
    fun onCategoryChange(v: Category) = _uiState.update { it.copy(category = v, categoryError = null) }
    fun onConditionChange(v: ItemCondition) = _uiState.update { it.copy(condition = v) }
    fun onWantedChange(v: String) = _uiState.update { it.copy(wantedItems = v, wantedError = null) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, addressError = null) }
    fun onImagePicked(uri: String) = _uiState.update { it.copy(imageUri = uri, imageError = null) }
    fun onLocationPicked(v: Location) = _uiState.update { it.copy(location = v) }

    fun publish() {
        val s = _uiState.value
        val wanted = s.wantedItems.split(",").map { it.trim() }.filter { it.isNotBlank() }

        val titleError = if (s.title.trim().length < 5) R.string.error_title_required else null
        val descriptionError = if (s.description.trim().length < 15) R.string.error_description_required else null
        val categoryError = if (s.category == null) R.string.error_category_required else null
        val wantedError = if (wanted.isEmpty()) R.string.error_wanted_required else null
        val addressError = if (s.address.isBlank()) R.string.error_address_required else null
        val imageError = if (s.imageUri == null) R.string.error_image_required else null

        if (listOf(titleError, descriptionError, categoryError, wantedError, addressError, imageError).any { it != null }) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    categoryError = categoryError,
                    wantedError = wantedError,
                    addressError = addressError,
                    imageError = imageError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }

            // 1. Subir la foto a la nube (Cloudinary)
            val imageUrl = imageStorage.upload(s.imageUri!!)
            if (imageUrl == null) {
                _uiState.update { it.copy(result = RequestResult.Failure(R.string.error_image_upload)) }
                return@launch
            }

            // 2. La IA revisa que la foto corresponda al objeto. Si la IA no responde,
            //    se deja publicar: igual queda PENDING y la revisa un moderador.
            val validation = imageValidator.validate(imageUrl, s.title.trim(), s.category!!)
            if (validation != null && !validation.relevant) {
                _uiState.update {
                    it.copy(result = RequestResult.Failure(R.string.error_image_not_relevant, listOf(validation.reason)))
                }
                return@launch
            }

            val user = userRepository.currentUser.value
            publicationRepository.save(
                Publication(
                    id = UUID.randomUUID().toString(),
                    title = s.title.trim(),
                    description = s.description.trim(),
                    category = s.category!!,
                    condition = s.condition,
                    wantedItems = wanted,
                    imageUrls = listOf(imageUrl),
                    location = s.location ?: user?.location ?: Location(4.5339, -75.6811),
                    address = s.address.trim(),
                    ownerId = user?.id.orEmpty(),
                    status = PublicationStatus.PENDING, // la revisa un moderador
                )
            )
            _uiState.update { it.copy(result = RequestResult.Success(R.string.create_success)) }
        }
    }

    fun resetResult() = _uiState.update { it.copy(result = null) }
}