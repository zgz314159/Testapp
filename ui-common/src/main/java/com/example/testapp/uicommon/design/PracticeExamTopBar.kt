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
    hasAnyAnalysis: Boolean,
    questionActionsEnabled: Boolean = true,
    aiActionsEnabled: Boolean = true,
) {
    val timerTitle = formatPracticeExamTimer(elapsedSeconds)
    val typographyLabel = stringResource(R.string.uicommon_typography_settings)
    val editQuestionLabel = stringResource(R.string.uicommon_edit_current_question)
    val iconSize = PracticeExamTopBarMetrics.iconButtonSize
    val tokens = AppElevatedActionSheetTokens

    Box {
        PracticeExamTopBarShell {
            AppTopBarIconButton(onClick = onRequestExit, size = iconSize) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = exitContentDescription,
                )
            }
            if (aiActionsEnabled) {
                AppTopBarIconButton(onClick = onAiMenuToggle, size = iconSize) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = aiParseLabel,
                        tint = if (hasAnyAnalysis) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            LocalContentColor.current
                        },
                    )
                }
            }
            Text(
                text = timerTitle,
                style = MaterialTheme.typography.titleMedium,
            )
            if (questionActionsEnabled) {
                AppTopBarIconButton(onClick = onToggleFavorite, size = iconSize) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) favoriteRemoveLabel else favoriteAddLabel,
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            LocalContentColor.current
                        },
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
                        },
                    )
                }
            }
            AppTopBarIconButton(onClick = onMenuToggle, size = iconSize) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsLabel)
            }
        }

        if (aiActionsEnabled) {
            PracticeExamAiDropdown(
                expanded = aiMenuExpanded,
                onDismiss = onAiMenuDismiss,
                deepSeekLabel = deepSeekLabel,
                sparkLabel = sparkLabel,
                onDeepSeek = onOpenAiMenu,
                onSparkAsk = onOpenAskMenu,
            )
        }

        AppElevatedActionSheet(
            visible = settingsMenuExpanded,
            title = stringResource(R.string.uicommon_more_action_sheet_title),
            subtitle = stringResource(R.string.uicommon_more_action_sheet_subtitle),
            onDismiss = onMenuDismiss,
            actions = buildList {
                add(
                    AppElevatedActionItem(
                        title = typographyLabel,
                        subtitle = stringResource(R.string.uicommon_typography_hint),
                        icon = Icons.Filled.FormatSize,
                        iconTint = tokens.brandBlue,
                        iconBg = tokens.brandBlueSoft,
                        onClick = {
                            onMenuDismiss()
                            onOpenTypography()
                        },
                    ),
                )
                if (questionActionsEnabled) {
                    add(
                        AppElevatedActionItem(
                            title = editQuestionLabel,
                            subtitle = stringResource(R.string.uicommon_edit_question_hint),
                            icon = Icons.Filled.Edit,
                            iconTint = tokens.accentTeal,
                            iconBg = tokens.accentTealSoft,
                            onClick = {
                                onMenuDismiss()
                                onEditQuestion()
                            },
                        ),
                    )
                }
            },
        )
    }
}
