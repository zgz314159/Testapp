package com.example.testapp.presentation.screen.ai

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.core.util.safeEncode
import com.example.testapp.feature.ai.R
import com.example.testapp.uicommon.component.ActionModeTextToolbar
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.layout.ArtifactFullscreenShell
import kotlinx.coroutines.launch

@Composable
fun DeepSeekScreen(
    text: String,
    questionId: Int,
    index: Int,
    onBack: () -> Unit = {},
    onOpenAsk: (encodedSelection: String) -> Unit = {},
    onSave: (String) -> Unit = {},
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
) {
    val typography = rememberAiArtifactTypography(AiFontScope.DEEPSEEK)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var menuExpanded by remember { mutableStateOf(false) }
    val settingsText = stringResource(R.string.settings)
    val increaseFontText = stringResource(R.string.increase_font)
    val decreaseFontText = stringResource(R.string.decrease_font)
    val saveText = stringResource(R.string.save)
    val cancelText = stringResource(R.string.cancel)
    val saveSuccessText = stringResource(R.string.save_success)
    val askMenuLabel = stringResource(R.string.ask)
    val editableTextState = remember { mutableStateOf(TextFieldValue(text)) }
    var editableText by editableTextState
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view, onOpenAsk, askMenuLabel) {
        ActionModeTextToolbar(
            view = view,
            onAIQuestion = {
                val sel = editableTextState.value.selection
                val selected = if (sel.min < sel.max) editableTextState.value.text.substring(sel.min, sel.max) else ""
                if (selected.isNotBlank()) {
                    onOpenAsk(safeEncode(selected))
                }
            },
            aiServiceName = "DeepSeek",
            askMenuLabel = askMenuLabel,
        )
    }

    BackHandler {
        if (editableText.text != text) {
            showSaveDialog = true
        } else {
            onBack()
        }
    }
    ArtifactFullscreenShell(
        topEndActions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsText)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text(increaseFontText) }, onClick = {
                    val next = (typography.fontSize.size + 2).coerceAtMost(32f)
                    typography.fontSize.setSize(next)
                    coroutineScope.launch { typography.fontSize.persistSize(next) }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(decreaseFontText) }, onClick = {
                    val next = (typography.fontSize.size - 2).coerceAtLeast(14f)
                    typography.fontSize.setSize(next)
                    coroutineScope.launch { typography.fontSize.persistSize(next) }
                    menuExpanded = false
                })
            }
        }
    ) { contentModifier ->
        Column(
            modifier = contentModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                BasicTextField(
                    value = editableText,
                    onValueChange = { editableText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = typography.fontSize.size.sp,
                        fontFamily = LocalFontFamily.current,
                        lineHeight = (typography.fontSize.size * typography.lineSpacing).sp,
                        letterSpacing = typography.letterSpacing.sp
                    )
                )
            }
        }
    }
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onSave(editableText.text)
                    aiViewModel.save(questionId, editableText.text)
                    Toast.makeText(context, saveSuccessText, Toast.LENGTH_SHORT).show()
                    showSaveDialog = false
                    onBack()
                }) { Text(saveText) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    onBack()
                }) { Text(cancelText) }
            },
            text = { Text(stringResource(R.string.confirm_save_changes)) }
        )
    }
}
