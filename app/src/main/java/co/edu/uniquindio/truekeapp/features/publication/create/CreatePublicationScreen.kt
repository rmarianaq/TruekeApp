package co.edu.uniquindio.truekeapp.features.publication.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.AuthHeader
import co.edu.uniquindio.truekeapp.core.component.LightSystemBarIcons
import co.edu.uniquindio.truekeapp.core.component.TruekePrimaryButton
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTextField
import co.edu.uniquindio.truekeapp.core.component.icon
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePublicationScreen(
    onBackClick: () -> Unit,
    onPublished: () -> Unit,
    viewModel: CreatePublicationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isLoading

    LaunchedEffect(uiState.result) {
        val result = uiState.result
        if (result is RequestResult.Success) {
            launch { snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = false) }
            delay(1800)
            onPublished()
            viewModel.resetResult()
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
                title = stringResource(R.string.create_title),
                subtitle = stringResource(R.string.create_subtitle),
                onBackClick = onBackClick,
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Vista previa de la foto
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                Text(
                    text = stringResource(R.string.create_photo_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp),
                )

                TruekeTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = stringResource(R.string.field_title),
                    leadingIcon = Icons.Rounded.Inventory2,
                    errorRes = uiState.titleError,
                    enabled = enabled,
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.field_description)) },
                    minLines = 3,
                    isError = uiState.descriptionError != null,
                    supportingText = uiState.descriptionError?.let { { Text(stringResource(it)) } },
                    enabled = enabled,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    ),
                )

                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.create_category), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.category == category,
                            onClick = { viewModel.onCategoryChange(category) },
                            label = { Text(stringResource(category.labelRes)) },
                            leadingIcon = { Icon(category.icon, null, Modifier.size(18.dp)) },
                            shape = CircleShape,
                            enabled = enabled,
                        )
                    }
                }
                uiState.categoryError?.let {
                    Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.create_condition), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemCondition.entries.forEach { condition ->
                        FilterChip(
                            selected = uiState.condition == condition,
                            onClick = { viewModel.onConditionChange(condition) },
                            label = { Text(stringResource(condition.labelRes)) },
                            shape = CircleShape,
                            enabled = enabled,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                TruekeTextField(
                    value = uiState.wantedItems,
                    onValueChange = viewModel::onWantedChange,
                    label = stringResource(R.string.field_wanted),
                    leadingIcon = Icons.Rounded.SwapHoriz,
                    errorRes = uiState.wantedError,
                    enabled = enabled,
                )
                TruekeTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = stringResource(R.string.field_address),
                    leadingIcon = Icons.Rounded.LocationOn,
                    errorRes = uiState.addressError,
                    onImeAction = { focusManager.clearFocus() },
                    enabled = enabled,
                )

                Spacer(Modifier.height(12.dp))
                TruekePrimaryButton(
                    text = stringResource(R.string.create_button),
                    isLoading = uiState.isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.publish()
                    },
                )
            }
        }
    }
}