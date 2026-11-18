package co.edu.uniquindio.truekeapp.features.publication.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.truekeapp.R
import co.edu.uniquindio.truekeapp.core.component.LocationPickerField
import co.edu.uniquindio.truekeapp.core.component.PhotoPickerField
import co.edu.uniquindio.truekeapp.core.component.TruekeSnackbarHost
import co.edu.uniquindio.truekeapp.core.component.TruekeTextField
import co.edu.uniquindio.truekeapp.core.component.TruekeTopBar
import co.edu.uniquindio.truekeapp.core.component.icon
import co.edu.uniquindio.truekeapp.core.component.labelRes
import co.edu.uniquindio.truekeapp.core.component.showTruekeSnackbar
import co.edu.uniquindio.truekeapp.core.util.RequestResult
import co.edu.uniquindio.truekeapp.domain.model.Category
import co.edu.uniquindio.truekeapp.domain.model.ItemCondition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPublicationScreen(
    onBackClick: () -> Unit,
    onDone: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: EditPublicationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val enabled = !uiState.isLoading

    LaunchedEffect(uiState.result) {
        when (val result = uiState.result) {
            is RequestResult.Success -> {
                launch { snackbarHostState.showTruekeSnackbar(context.getString(result.messageRes), isError = false) }
                when (uiState.lastAction) {
                    EditAction.SAVED -> { delay(1200); onDone() }
                    EditAction.DELETED -> { delay(1000); onDeleted() }
                    else -> Unit
                }
                if (uiState.lastAction != EditAction.STATUS_CHANGED) viewModel.resetResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showTruekeSnackbar(
                    context.getString(result.messageRes, *result.args.toTypedArray()),
                    isError = true,
                )
                viewModel.resetResult()
            }
            else -> Unit
        }
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TruekeTopBar(title = stringResource(R.string.edit_title), onBackClick = onBackClick) },
        snackbarHost = { TruekeSnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (uiState.canToggleFinished) {
                        val finishText = stringResource(if (uiState.isFinished) R.string.edit_reopen else R.string.edit_mark_finished)
                        if (uiState.isFinished) {
                            OutlinedButton(onClick = viewModel::toggleFinished, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Rounded.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(finishText)
                            }
                        } else {
                            Button(
                                onClick = viewModel::toggleFinished,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary,
                                ),
                            ) {
                                Icon(Icons.Rounded.TaskAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(finishText)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) { Text(stringResource(R.string.edit_delete)) }
                        Button(
                            onClick = viewModel::save,
                            enabled = enabled,
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                        ) { Text(stringResource(R.string.edit_save), style = MaterialTheme.typography.titleSmall) }
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (uiState.isFinished) {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.edit_finished_banner),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // Foto actual (Cloudinary) o la nueva que se elija; si cambia, se sube y la valida la IA
            PhotoPickerField(
                image = uiState.newImageUri ?: uiState.original?.imageUrls?.firstOrNull(),
                onImagePicked = viewModel::onImagePicked,
                errorRes = null,
                enabled = enabled,
            )
            Spacer(Modifier.height(8.dp))

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
                colors = fieldColors,
            )

            Text(stringResource(R.string.create_category), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(stringResource(category.labelRes)) },
                        leadingIcon = { Icon(category.icon, null, Modifier.size(18.dp)) },
                        shape = CircleShape,
                        colors = chipColors,
                        enabled = enabled,
                    )
                }
            }

            Text(stringResource(R.string.create_condition), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemCondition.entries.forEach { condition ->
                    FilterChip(
                        selected = uiState.condition == condition,
                        onClick = { viewModel.onConditionChange(condition) },
                        label = { Text(stringResource(condition.labelRes)) },
                        shape = CircleShape,
                        colors = chipColors,
                        enabled = enabled,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
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
                enabled = enabled,
            )

            // Punto de intercambio en el mapa (Mapbox)
            Spacer(Modifier.height(8.dp))
            if (uiState.original != null) {
                LocationPickerField(
                    location = uiState.location,
                    initialCenter = uiState.location,
                    onLocationPicked = viewModel::onLocationPicked,
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.edit_delete_title)) },
            text = { Text(stringResource(R.string.edit_delete_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.edit_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
