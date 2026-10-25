package co.edu.uniquindio.truekeapp.features.moderation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.features.auth.login.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Reutiliza el LoginViewModel: la única diferencia es que exige el rol MODERATOR
@Composable
fun ModeratorLoginScreen(
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                val message = context.getString(result.messageRes, *result.args.toTypedArray())
                launch { snackbarHostState.showTruekeSnackbar(message, isError = false) }
                delay(1200)
                onLoginSuccess()
                viewModel.resetResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = true)
                viewModel.resetResult()
            }
            else -> Unit
        }
    }

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
                title = stringResource(R.string.moderator_login_title),
                subtitle = stringResource(R.string.moderator_login_subtitle),
                onBackClick = onBackClick,
            )
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TruekeTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = stringResource(R.string.moderator_email),
                    leadingIcon = Icons.Rounded.AdminPanelSettings,
                    errorRes = uiState.emailError,
                    keyboardType = KeyboardType.Email,
                    enabled = !uiState.isLoading,
                )
                TruekeTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = stringResource(R.string.field_password),
                    leadingIcon = Icons.Rounded.Lock,
                    errorRes = uiState.passwordError,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.login(asModerator = true)
                    },
                    enabled = !uiState.isLoading,
                )
                TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.login_forgot_password))
                }
                Spacer(Modifier.height(8.dp))
                TruekePrimaryButton(
                    text = stringResource(R.string.moderator_login_button),
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.login(asModerator = true)
                    },
                )
                TextButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.moderator_back_home))
                }
            }
        }
    }
}
