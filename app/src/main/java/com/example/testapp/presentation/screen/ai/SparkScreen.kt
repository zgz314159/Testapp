package com.example.testapp.presentation.screen.ai

import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.layout.ArtifactFullscreenShell
import com.example.testapp.uicommon.component.LocalFontSize
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

@Composable
fun SparkScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: (String) -> Unit = {},
    sparkViewModel: SparkViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getSparkFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    val storedLineSpacing by FontSettingsDataStore
        .getPracticeLineSpacing(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    val storedLetterSpacing by FontSettingsDataStore
        .getPracticeLetterSpacing(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var screenLineSpacing by remember { mutableStateOf(1.3f) }
    var screenLetterSpacing by remember { mutableStateOf(0f) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(storedLineSpacing) {
        if (!storedLineSpacing.isNaN()) screenLineSpacing = storedLineSpacing
    }
    LaunchedEffect(storedLetterSpacing) {
        if (!storedLetterSpacing.isNaN()) screenLetterSpacing = storedLetterSpacing
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val settingsText = stringResource(R.string.settings)
    val increaseFontText = stringResource(R.string.increase_font)
    val decreaseFontText = stringResource(R.string.decrease_font)
    val saveText = stringResource(R.string.save)
    val cancelText = stringResource(R.string.cancel)
    val saveSuccessText = stringResource(R.string.save_success)
    val editableTextState = remember { mutableStateOf(TextFieldValue(text)) }
    var editableText by editableTextState
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view, navController) {
        ActionModeTextToolbar(
            view = view, 
            onAIQuestion = {
                val sel = editableTextState.value.selection
                val selected = if (sel.min < sel.max) editableTextState.value.text.substring(sel.min, sel.max) else ""
                if (selected.isNotBlank()) {
                    val encoded = com.example.testapp.util.safeEncode(selected)
                    navController?.navigate("spark_ask/$questionId/$index/$encoded")
                }
            },
            aiServiceName = "Spark"
        )
    }

    BackHandler {
        if (editableText.text != text) {
            showSaveDialog = true
        } else {
            navController?.popBackStack()
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
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(decreaseFontText) }, onClick = {
                    screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
                    }
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
                        fontSize = screenFontSize.sp,
                        fontFamily = LocalFontFamily.current,
                        lineHeight = (screenFontSize * screenLineSpacing).sp,
                        letterSpacing = screenLetterSpacing.sp
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
                    sparkViewModel.save(questionId, editableText.text)
                    Toast.makeText(context, saveSuccessText, Toast.LENGTH_SHORT).show()
                    showSaveDialog = false
                    navController?.popBackStack()
                }) { Text(saveText) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    navController?.popBackStack()
                }) { Text(cancelText) }
            },
            text = { Text(stringResource(R.string.confirm_save_changes)) }
        )
    }
}

