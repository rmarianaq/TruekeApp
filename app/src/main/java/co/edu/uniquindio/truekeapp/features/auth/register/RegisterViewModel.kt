package co.edu.uniquindio.truekeapp.features.auth.register

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.Validators
import co.edu.uniquindio.truekeapp.domain.model.User
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val address: String = "",
    val acceptedTerms: Boolean = false,
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    @StringRes val addressError: Int? = null,
    val termsError: Boolean = false,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, addressError = null) }
    fun onTermsChange(value: Boolean) = _uiState.update { it.copy(acceptedTerms = value, termsError = false) }

    fun register() {
        val state = _uiState.value

        // 1. Validar todos los campos a la vez, para mostrar todos los errores juntos
        val nameError = if (state.name.trim().length < 3) R.string.error_name_required else null
        val emailError = Validators.email(state.email)
        val passwordError = Validators.password(state.password)
        val confirmError = when {
            state.confirmPassword.isBlank() -> R.string.error_confirm_required
            state.confirmPassword != state.password -> R.string.error_password_mismatch
            else -> null
        }
        val addressError = if (state.address.isBlank()) R.string.error_address_required else null
        val termsError = !state.acceptedTerms

        val hasErrors = listOf(nameError, emailError, passwordError, confirmError, addressError)
            .any { it != null } || termsError

        if (hasErrors) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                    addressError = addressError,
                    termsError = termsError,
                )
            }
            return
        }

        // 2. Crear la cuenta
        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            delay(1200)

            // El correo debe ser único
            if (userRepository.findByEmail(state.email) != null) {
                _uiState.update {
                    it.copy(
                        emailError = R.string.register_error_email_taken,
                        result = RequestResult.Failure(R.string.register_error_email_taken),
                    )
                }
                return@launch
            }

            val user = User(
                id = UUID.randomUUID().toString(),
                name = state.name.trim(),
                email = state.email.trim().lowercase(),
                password = state.password,
                address = state.address.trim(),
            )
            userRepository.save(user)

            _uiState.update {
                it.copy(result = RequestResult.Success(R.string.register_success, listOf(user.name.substringBefore(" "))))
            }
        }
    }

    fun resetResult() = _uiState.update { it.copy(result = null) }
}