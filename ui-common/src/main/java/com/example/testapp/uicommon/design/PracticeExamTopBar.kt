package com.example.testapp.uicommon.design

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.uicommon.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeExamTopBar(
    elapsedSeconds: Int,
    onRequestExit: () -> Unit,
    exitContentDescription: String,
    isFavorite: Boolean,
    favoriteAddLabel: String,
    favoriteRemoveLabel: String,
    notesLabel: String,
    onEditNote: () -> Unit,
    hasNote: Boolean,
    aiParseLabel: String,
    deepSeekLabel: String,
    sparkLabel: String,
    aiMenuExpanded: Boolean,
    onAiMenuToggle: () -> Unit,
    onAiMenuDismiss: () -> Unit,
    onOpenAiMenu: () -> Unit,
    onOpenAskMenu: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenTypography: () -> Unit,
    onEditQuestion: () -> Unit,
    settingsLabel: String,
    settingsMenuExpanded: Boolean,
    onMenuToggle: () -> Unit,
    onMenuDismiss: () -> Unit,
    hasAnyAnalysis: Boolean
) {
    val timerTitle = formatPracticeExamTimer(elapsedSeconds)
    val typographyLabel = stringResource(R.string.uicommon_typography_settings)
    val editQuestionLabel = stringResource(R.string.uicommon_edit_current_question)
    val iconSize = PracticeExamTopBarMetrics.iconButtonSize

    PracticeExamTopBarShell {
        AppTopBarIconButton(onClick = onRequestExit, size = iconSize) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = exitContentDescription
            )
        }
        Box {
            AppTopBarIconButton(onClick = onAiMenuToggle, size = iconSize) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = aiParseLabel,
                    tint = if (hasAnyAnalysis) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalContentColor.current
                    }
                )
            }
            PracticeExamAiDropdown(
                expanded = aiMenuExpanded,
                onDismiss = onAiMenuDismiss,
                deepSeekLabel = deepSeekLabel,
                sparkLabel = sparkLabel,
                onDeepSeek = onOpenAiMenu,
                onSparkAsk = onOpenAskMenu
            )
        }
        Text(
            text = timerTitle,
            style = MaterialTheme.typography.titleMedium
        )
        AppTopBarIconButton(onClick = onToggleFavorite, size = iconSize) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isFavorite) favoriteRemoveLabel else favoriteAddLabel,
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    LocalContentColor.current
                }
            )
        }
        AppTopBarIconButton(onClick = onEditNote, size = iconSize) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
                contentDescription = notesLabel,
                tint = if (hasNote) {
                    MaterialTheme.colorScheme.primary
                } else {
                    LocalContentColor.current
                }
            )
        }
        Box {
            AppTopBarIconButton(onClick = onMenuToggle, size = iconSize) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsLabel)
            }
            DropdownMenu(expanded = settingsMenuExpanded, onDismissRequest = onMenuDismiss) {
                DropdownMenuItem(
                    text = { Text(typographyLabel) },
                    onClick = {
                        onMenuDismiss()
                        onOpenTypography()
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.FormatSize, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(editQuestionLabel) },
                    onClick = {
                        onMenuDismiss()
                        onEditQuestion()
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                    }
                )
            }
        }
    }
}
