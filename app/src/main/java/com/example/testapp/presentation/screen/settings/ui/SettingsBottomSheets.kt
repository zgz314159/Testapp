package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.R
import com.example.testapp.uicommon.design.AppEmptyStateInline
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsExportBottomSheet(
    title: String,
    fileNames: List<String>,
    emptyMessage: String,
    fontSize: Float,
    onSelectFile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.lg)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                style = MaterialTheme.typography.titleMedium
            )
            if (fileNames.isEmpty()) {
                AppEmptyStateInline(
                    message = emptyMessage,
                    modifier = Modifier.padding(horizontal = AppSpacing.lg)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    fileNames.forEach { fileName ->
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFile(fileName) },
                            headlineContent = {
                                SettingsHeadlineText(fileName, fontSize)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsImportQuizBottomSheet(
    onImportFile: () -> Unit,
    onImportLocal: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.lg)
        ) {
            Text(
                text = stringResource(R.string.settings_import_quiz_choice_title),
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                style = MaterialTheme.typography.titleMedium
            )
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onImportFile()
                    },
                leadingContent = {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                },
                headlineContent = { Text(stringResource(R.string.settings_import_quiz_file)) }
            )
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onImportLocal()
                    },
                leadingContent = {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                },
                headlineContent = { Text(stringResource(R.string.settings_import_quiz_local)) }
            )
        }
    }
}
