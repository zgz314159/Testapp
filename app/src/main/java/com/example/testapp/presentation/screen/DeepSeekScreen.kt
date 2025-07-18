package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavController
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.ActionModeTextToolbar
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DeepSeekScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: (String) -> Unit = {},
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getDeepSeekFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val editableTextState = remember { mutableStateOf(TextFieldValue(text)) }
    var editableText by editableTextState
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view, navController) {
        ActionModeTextToolbar(view) {
            val sel = editableTextState.value.selection
            val selected = if (sel.min < sel.max) editableTextState.value.text.substring(sel.min, sel.max) else ""
            if (selected.isNotBlank()) {
                val encoded = java.net.URLEncoder.encode(selected, "UTF-8")
                navController?.navigate("deepseek_ask/$encoded")
            }
        }
    }

    BackHandler {
        if (editableText.text != text) {
            showSaveDialog = true
        } else {
            navController?.popBackStack()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                BasicTextField(
                    value = editableText,
                    onValueChange = { editableText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = screenFontSize.sp, fontFamily = LocalFontFamily.current)
                )
            }

        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "设置")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text("放大字体") }, onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
            }
        }
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        onSave(editableText.text)
                        aiViewModel.save(questionId, editableText.text)
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("保存") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("取消") }
                },
                text = { Text("是否保存修改？") }
            )
        }
      
    }
}