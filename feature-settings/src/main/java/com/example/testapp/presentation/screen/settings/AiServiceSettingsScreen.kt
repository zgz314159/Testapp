package com.example.testapp.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.feature.settings.R
import com.example.testapp.presentation.screen.settings.ui.SettingsCardGroup
import com.example.testapp.presentation.screen.settings.ui.SettingsSectionHeader
import com.example.testapp.presentation.screen.settings.ui.SettingsTopBar
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiServiceSettingsScreen(
    onBack: () -> Unit,
    viewModel: AiServiceSettingsViewModel = hiltViewModel(),
) {
    val status by viewModel.status.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.consumeMessage()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppElevatedActionSheetTokens.sheetBg,
        topBar = {
            SettingsTopBar(
                title = stringResource(R.string.settings_ai_service),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = AppSpacing.md),
        ) {
            SettingsSectionHeader(stringResource(R.string.settings_ai_byok_section))
            Text(
                text = stringResource(R.string.settings_ai_byok_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            SettingsCardGroup {
                Column(modifier = Modifier.padding(16.dp)) {
                    ApiKeySection(
                        label = stringResource(R.string.settings_ai_deepseek_label),
                        inputLabel = stringResource(R.string.settings_ai_deepseek_input),
                        configured = status.deepSeekConfigured,
                        hint = status.deepSeekHint,
                        onSave = viewModel::saveDeepSeekKey,
                        onClear = viewModel::clearDeepSeekKey,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ApiKeySection(
                        label = stringResource(R.string.settings_ai_bocha_label),
                        inputLabel = stringResource(R.string.settings_ai_bocha_input),
                        configured = status.bochaConfigured,
                        hint = status.bochaHint,
                        onSave = viewModel::saveBochaKey,
                        onClear = viewModel::clearBochaKey,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ApiKeySection(
                        label = stringResource(R.string.settings_ai_tavily_label),
                        inputLabel = stringResource(R.string.settings_ai_tavily_input),
                        configured = status.tavilyConfigured,
                        hint = status.tavilyHint,
                        onSave = viewModel::saveTavilyKey,
                        onClear = viewModel::clearTavilyKey,
                    )
                }
            }

            SettingsSectionHeader(stringResource(R.string.settings_ai_managed_section))
            SettingsCardGroup {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_ai_managed_help),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.settings_ai_purchase_coming_soon))
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeySection(
    label: String,
    inputLabel: String,
    configured: Boolean,
    hint: String,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    KeyStatusRow(label = label, configured = configured, hint = hint)
    Spacer(modifier = Modifier.height(8.dp))
    ApiKeyField(
        value = draft,
        onValueChange = { draft = it },
        visible = visible,
        onToggleVisible = { visible = !visible },
        label = inputLabel,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = {
                onClear()
                draft = ""
            },
            enabled = configured,
        ) {
            Text(stringResource(R.string.settings_ai_clear))
        }
        Button(
            onClick = {
                onSave(draft)
                draft = ""
                visible = false
            },
            enabled = draft.isNotBlank(),
        ) {
            Text(stringResource(R.string.settings_ai_save))
        }
    }
}

@Composable
private fun KeyStatusRow(
    label: String,
    configured: Boolean,
    hint: String,
) {
    Text(text = label, style = MaterialTheme.typography.titleSmall)
    Text(
        text = if (configured) {
            stringResource(R.string.settings_ai_configured_format, hint)
        } else {
            stringResource(R.string.settings_ai_not_configured)
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (configured) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        },
    )
}

@Composable
private fun ApiKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = null,
                )
            }
        },
    )
}
