package co.edu.uniquindio.truekeapp.features.auth.recoverpass

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.core.util.Validators
import co.edu.uniquindio.truekeapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RESEND_SECONDS = 30

data class RecoverPasswordUiState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    val emailSent: Boolean = false,
    val resendSeconds: Int = 0,
    val result: RequestResult? = null,
) {
    val isLoading: Boolean get() = result is RequestResult.Loading
    val canResend: Boolean get() = resendSeconds == 0 && !isLoading
}

@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null) }

    fun sendLink() {
        val emailError = Validators.email(_uiState.value.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(result = RequestResult.Loading) }
            delay(1200)

            userRepository.sendPasswordResetEmail(_uiState.value.email.trim())

            _uiState.update {
                it.copy(emailSent = true, result = RequestResult.Success(R.string.recover_success))
            }
            startResendCountdown()
        }
    }

    fun resend() {
        if (_uiState.value.canResend) sendLink()
    }

    // Vuelve al formulario para corregir el correo
    fun useOtherEmail() {
        countdownJob?.cancel()
        _uiState.update { it.copy(emailSent = false, resendSeconds = 0) }
    }

    fun resetResult() = _uiState.update { it.copy(result = null) }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in RESEND_SECONDS downTo 0) {
                _uiState.update { it.copy(resendSeconds = seconds) }
                if (seconds > 0) delay(1000)
            }
        }
    }
}