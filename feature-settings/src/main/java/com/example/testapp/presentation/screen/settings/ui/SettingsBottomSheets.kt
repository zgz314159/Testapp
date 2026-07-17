package com.example.testapp.presentation.screen.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testapp.feature.settings.R
import com.example.testapp.uicommon.design.AppEmptyStateInline
import com.example.testapp.uicommon.design.AppOverlayMetrics
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.AppStaticBottomSheet
import com.example.testapp.uicommon.design.appOverlayContainerColor

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
    AppStaticBottomSheet(onDismiss = onDismiss) {
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
                    .padding(horizontal = AppSpacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                fileNames.forEach { fileName ->
                    ElevatedCard(
                        onClick = { onSelectFile(fileName) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppOverlayMetrics.listItemCorner),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = appOverlayContainerColor(),
                        ),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = AppOverlayMetrics.listItemElevation,
                        ),
                    ) {
                        SettingsHeadlineText(
                            text = fileName,
                            fontSize = fontSize,
                            modifier = Modifier.padding(
                                horizontal = AppSpacing.md,
                                vertical = AppSpacing.md,
                            ),
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
    AppStaticBottomSheet(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_import_quiz_choice_title),
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            style = MaterialTheme.typography.titleMedium
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ElevatedCard(
                onClick = {
                    onDismiss()
                    onImportFile()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppOverlayMetrics.listItemCorner),
                colors = CardDefaults.elevatedCardColors(containerColor = appOverlayContainerColor()),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = AppOverlayMetrics.listItemElevation,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Text(stringResource(R.string.settings_import_quiz_file))
                }
            }
            ElevatedCard(
                onClick = {
                    onDismiss()
                    onImportLocal()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppOverlayMetrics.listItemCorner),
                colors = CardDefaults.elevatedCardColors(containerColor = appOverlayContainerColor()),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = AppOverlayMetrics.listItemElevation,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Text(stringResource(R.string.settings_import_quiz_local))
                }
            }
        }
    }
}
