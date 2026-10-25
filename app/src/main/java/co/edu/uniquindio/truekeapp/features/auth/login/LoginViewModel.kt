package co.edu.uniquindio.truekeapp.features.auth.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.Validators
import co.edu.uniquindio.truekeapp.domain.model.UserRole
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Al escribir se borra el error de ese campo: el usuario ya lo está corrigiendo
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    // asModerator = true cuando se usa desde el acceso de moderadores
    fun login(asModerator: Boolean = false) {
        val state = _uiState.value

        // 1. Validar antes de llamar al repositorio
        val emailError = Validators.email(state.email)
        val passwordError = if (state.password.isBlank()) R.string.error_password_required else null

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        // 2. Intentar iniciar sesión
        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            delay(1200) // simula la espera de un servidor real

            val user = userRepository.login(state.email, state.password)

            // Cada rol entra por su propia puerta, como en los mockups
            val wrongDoor = user != null && (user.role == UserRole.MODERATOR) != asModerator
            if (wrongDoor) userRepository.logout()

            _uiState.update {
                it.copy(
                    result = when {
                        user == null -> RequestResult.Failure(R.string.login_error_invalid)
                        wrongDoor && asModerator -> RequestResult.Failure(R.string.moderator_error_not_moderator)
                        wrongDoor -> RequestResult.Failure(R.string.login_error_use_moderator)
                        else -> RequestResult.Success(R.string.login_success, listOf(user.name.substringBefore(" ")))
                    }
                )
            }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(result = null) }
    }
}