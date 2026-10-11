package co.edu.uniquindio.truekeapp.features.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.AuthHeader
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.PasswordStrengthIndicator
import co.edu.uniquindio.truekeapp.core.component.TruekePrimaryButton
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTextField
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.theme.TruekeAppTheme
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                val message = context.getString(result.messageRes, *result.args.toTypedArray())
                launch { snackbarHostState.showTruekeSnackbar(message, isError = false) }
                delay(1800)
                onRegisterSuccess()
                viewModel.resetResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = true)
                viewModel.resetResult()
            }
            else -> Unit
        }
    }

    RegisterContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onAddressChange = viewModel::onAddressChange,
        onTermsChange = viewModel::onTermsChange,
        onRegisterClick = viewModel::register,
        onBackClick = onBackClick,
        onLoginClick = onLoginClick,
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    LightSystemBarIcons()
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isLoading

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
                title = stringResource(R.string.register_title),
                subtitle = stringResource(R.string.register_subtitle),
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TruekeTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.field_name),
                    leadingIcon = Icons.Rounded.Person,
                    errorRes = uiState.nameError,
                    enabled = enabled,
                )
                TruekeTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.field_email),
                    leadingIcon = Icons.Rounded.Email,
                    errorRes = uiState.emailError,
                    keyboardType = KeyboardType.Email,
                    enabled = enabled,
                )
                TruekeTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.field_password),
                    leadingIcon = Icons.Rounded.Lock,
                    errorRes = uiState.passwordError,
                    isPassword = true,
                    enabled = enabled,
                )
                PasswordStrengthIndicator(password = uiState.password)
                Spacer(Modifier.height(4.dp))
                TruekeTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(R.string.field_confirm_password),
                    leadingIcon = Icons.Rounded.LockReset,
                    errorRes = uiState.confirmPasswordError,
                    isPassword = true,
                    enabled = enabled,
                )
                TruekeTextField(
                    value = uiState.address,
                    onValueChange = onAddressChange,
                    label = stringResource(R.string.field_address),
                    leadingIcon = Icons.Rounded.LocationOn,
                    errorRes = uiState.addressError,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() },
                    enabled = enabled,
                )

                TermsCheckbox(
                    checked = uiState.acceptedTerms,
                    isError = uiState.termsError,
                    onCheckedChange = onTermsChange,
                    enabled = enabled,
                )

                Spacer(Modifier.height(12.dp))

                TruekePrimaryButton(
                    text = stringResource(R.string.register_button),
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        onRegisterClick()
                    },
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.register_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onLoginClick) {
                        Text(stringResource(R.string.register_login))
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsCheckbox(
    checked: Boolean,
    isError: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    val errorColor = MaterialTheme.colorScheme.error

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                // Toda la fila es tocable, no solo el cuadrito
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                )
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,   // el clic lo maneja la fila completa
                enabled = enabled,
                colors = if (isError) CheckboxDefaults.colors(uncheckedColor = errorColor) else CheckboxDefaults.colors(),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.register_terms),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) errorColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (isError) {
            Text(
                text = stringResource(R.string.register_terms_error),
                style = MaterialTheme.typography.bodySmall,
                color = errorColor,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterPreview() {
    TruekeAppTheme {
        RegisterContent(
            uiState = RegisterUiState(
                name = "Mariana",
                password = "Trueke12",
                confirmPassword = "Trueke",
                confirmPasswordError = R.string.error_password_mismatch,
                termsError = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onAddressChange = {},
            onTermsChange = {},
            onRegisterClick = {},
            onBackClick = {},
            onLoginClick = {},
        )
    }
}