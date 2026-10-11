package co.edu.uniquindio.truekeapp.features.auth.recoverpass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.AuthHeader
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.TruekePrimaryButton
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTextField
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import co.edu.uniquindio.truekeapp.core.util.RequestResult

@Composable
fun RecoverPasswordScreen(
    onBackClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    viewModel: RecoverPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = false)
                viewModel.resetResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = true)
                viewModel.resetResult()
            }
            else -> Unit
        }
    }

    RecoverPasswordContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEmailChange = viewModel::onEmailChange,
        onSendClick = viewModel::sendLink,
        onResendClick = viewModel::resend,
        onUseOtherEmailClick = viewModel::useOtherEmail,
        onBackClick = onBackClick,
        onBackToLoginClick = onBackToLoginClick,
    )
}

@Composable
private fun RecoverPasswordContent(
    uiState: RecoverPasswordUiState,
    snackbarHostState: SnackbarHostState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onResendClick: () -> Unit,
    onUseOtherEmailClick: () -> Unit,
    onBackClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
) {
    LightSystemBarIcons()

    Scaffold(
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            AuthHeader(
                title = stringResource(R.string.recover_title),
                subtitle = stringResource(R.string.recover_subtitle),
                onBackClick = onBackClick,
            )

            // Cambia con animación entre el formulario y la confirmación
            AnimatedContent(
                targetState = uiState.emailSent,
                transitionSpec = {
                    (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 6 }) togetherWith
                            fadeOut(tween(200))
                },
                label = "recoverContent",
            ) { sent ->
                if (sent) {
                    EmailSentContent(
                        email = uiState.email.trim(),
                        resendSeconds = uiState.resendSeconds,
                        canResend = uiState.canResend,
                        onBackToLoginClick = onBackToLoginClick,
                        onResendClick = onResendClick,
                        onUseOtherEmailClick = onUseOtherEmailClick,
                    )
                } else {
                    RecoverForm(
                        uiState = uiState,
                        onEmailChange = onEmailChange,
                        onSendClick = onSendClick,
                        onBackToLoginClick = onBackToLoginClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecoverForm(
    uiState: RecoverPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TruekeTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            leadingIcon = Icons.Rounded.Email,
            errorRes = uiState.emailError,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                onSendClick()
            },
            enabled = !uiState.isLoading,
        )
        Spacer(Modifier.height(12.dp))
        TruekePrimaryButton(
            text = stringResource(R.string.recover_button),
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                onSendClick()
            },
        )
        TextButton(
            onClick = onBackToLoginClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.recover_back_to_login))
        }
    }
}

@Composable
private fun EmailSentContent(
    email: String,
    resendSeconds: Int,
    canResend: Boolean,
    onBackToLoginClick: () -> Unit,
    onResendClick: () -> Unit,
    onUseOtherEmailClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MarkEmailRead,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.recover_sent_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.recover_sent_message, email),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        TruekePrimaryButton(
            text = stringResource(R.string.recover_back_to_login),
            onClick = onBackToLoginClick,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onResendClick, enabled = canResend) {
            Text(
                if (resendSeconds > 0) {
                    stringResource(R.string.recover_resend_in, resendSeconds)
                } else {
                    stringResource(R.string.recover_resend)
                }
            )
        }
        TextButton(onClick = onUseOtherEmailClick) {
            Text(stringResource(R.string.recover_use_other_email))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RecoverPasswordSentPreview() {
    TruekeAppTheme {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(email = "mariana@trueke.co", emailSent = true, resendSeconds = 24),
            snackbarHostState = remember { SnackbarHostState() },
            onEmailChange = {},
            onSendClick = {},
            onResendClick = {},
            onUseOtherEmailClick = {},
            onBackClick = {},
            onBackToLoginClick = {},
        )
    }
}