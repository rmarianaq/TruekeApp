package co.edu.uniquindio.truekeapp.features.profile.edit

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.Validators
import co.edu.uniquindio.truekeapp.domain.repository.PublicationRepository
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val bio: String = "",
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val addressError: Int? = null,
    val accountDeleted: Boolean = false,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val publicationRepository: PublicationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        // Precarga el formulario con los datos actuales del usuario
        userRepository.currentUser.value?.let { user ->
            _uiState.update {
                it.copy(name = user.name, email = user.email, address = user.address, bio = user.bio)
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onAddressChange(v: String) = _uiState.update { it.copy(address = v, addressError = null) }
    fun onBioChange(v: String) = _uiState.update { it.copy(bio = v) }

    fun save() {
        val me = userRepository.currentUser.value ?: return
        val s = _uiState.value

        val nameError = if (s.name.trim().length < 3) R.string.error_name_required else null
        val emailError = Validators.email(s.email)
        val addressError = if (s.address.isBlank()) R.string.error_address_required else null

        if (nameError != null || emailError != null || addressError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError, addressError = addressError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            delay(800)

            // El correo sigue siendo único: nadie más puede tenerlo
            val owner = userRepository.findByEmail(s.email)
            if (owner != null && owner.id != me.id) {
                _uiState.update {
                    it.copy(
                        emailError = R.string.register_error_email_taken,
                        result = RequestResult.Failure(R.string.register_error_email_taken),
                    )
                }
                return@launch
            }

            userRepository.save(
                me.copy(
                    name = s.name.trim(),
                    email = s.email.trim().lowercase(),
                    address = s.address.trim(),
                    bio = s.bio.trim(),
                )
            )
            _uiState.update { it.copy(result = RequestResult.Success(R.string.edit_profile_saved)) }
        }
    }

    // Borra la cuenta y todas sus publicaciones; luego la sesión queda cerrada
    fun deleteAccount() {
        val me = userRepository.currentUser.value ?: return
        viewModelScope.launch {
            publicationRepository.deleteByOwner(me.id)
            userRepository.delete(me.id)
            _uiState.update { it.copy(accountDeleted = true) }
        }
    }

    fun resetResult() = _uiState.update { it.copy(result = null) }
}
