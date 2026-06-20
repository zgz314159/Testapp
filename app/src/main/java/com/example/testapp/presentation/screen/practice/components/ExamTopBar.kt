package com.example.testapp.presentation.screen.practice.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

@Composable
fun ExamTopBar(
    elapsed: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    aiMenuExpanded: Boolean,
    onAiMenuToggle: () -> Unit,
    onAiMenuDismiss: () -> Unit,
    onOpenAiMenu: () -> Unit,
    onOpenAskMenu: () -> Unit,
    onEditNote: () -> Unit,
    onShowList: () -> Unit,
    settingsMenuExpanded: Boolean,
    onMenuToggle: () -> Unit,
    onMenuDismiss: () -> Unit,
    questionsSize: Int,
    hasAnyAnalysis: Boolean,
    hasNote: Boolean,
    settingsMenuContent: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            "%02d:%02d".format(elapsed / 60, elapsed % 60),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (isFavorite) stringResource(R.string.favorite_remove) else stringResource(R.string.favorite_add)
            )
        }
        Box {
            IconButton(onClick = onAiMenuToggle) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = stringResource(R.string.ai_parse),
                    tint = if (hasAnyAnalysis) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            DropdownMenu(expanded = aiMenuExpanded, onDismissRequest = onAiMenuDismiss) {
                DropdownMenuItem(text = { Text(stringResource(R.string.ai_name_deepseek)) }, onClick = { onAiMenuDismiss(); onOpenAiMenu() })
                DropdownMenuItem(text = { Text(stringResource(R.string.ai_name_spark)) }, onClick = { onAiMenuDismiss(); onOpenAskMenu() })
            }
        }
        IconButton(onClick = onEditNote) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.notes),
                tint = if (hasNote) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Card(onClick = onShowList) {
            Text(
                stringResource(R.string.total_questions, questionsSize),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        Box {
            IconButton(onClick = onMenuToggle) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.settings))
            }
            DropdownMenu(expanded = settingsMenuExpanded, onDismissRequest = onMenuDismiss) {
                settingsMenuContent()
            }
        }
    }
}

