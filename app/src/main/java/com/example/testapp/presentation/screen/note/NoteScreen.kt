package com.example.testapp.presentation.screen.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.exam.ExamViewModel
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.testapp.R

@Composable
fun NoteScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: (String) -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    examViewModel: ExamViewModel? = null,
    practiceViewModel: PracticeViewModel? = null
) {
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    
    // 监听 noteList 变化并实时更新界面 - 使用直接 StateFlow 订阅
    val examNoteList by (examViewModel?.noteList?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val practiceNoteList by (practiceViewModel?.noteList?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    
    // 强制刷新触发器，用于同步 StateFlow 值到界面
    var forceRefreshTrigger by remember { mutableStateOf(0) }
    
    // 调试日志：追踪 StateFlow 变化
    LaunchedEffect(examNoteList) {
        
    }
    
    LaunchedEffect(practiceNoteList) {
        
    }
    
    // 强制刷新机制 - 每次进入 NoteScreen 时强制加载最新 StateFlow 值
    LaunchedEffect(Unit) {
        
        forceRefreshTrigger = forceRefreshTrigger + 1
        
        // 直接从 ViewModel 获取最新值并打印
        practiceViewModel?.noteList?.value?.let { list ->
            
            if (index < list.size) {
                
            }
        }
        
        examViewModel?.noteList?.value?.let { list ->
            
            if (index < list.size) {
                
            }
        }
    }
    
    // 调试 ViewModel 实例日志
    LaunchedEffect(Unit) {

        // 强制触发 StateFlow 刷新，确保界面首次显示正确
        
    }
    
    // 获取当前 index 对应的实时 note 内容 - 使用强制 StateFlow 订阅
    val currentNote = remember(practiceNoteList, examNoteList, index, forceRefreshTrigger) {
        // 优先尝试直接从 ViewModel 的 StateFlow 获取最新值
        val directPracticeNote = practiceViewModel?.noteList?.value?.let { list ->
            if (index < list.size) {

                list[index]
            } else null
        }
        
        val directExamNote = examViewModel?.noteList?.value?.let { list ->
            if (index < list.size) {

                list[index]
            } else null
        }
        
        // 否则使用直接访问的值，回退到 collectAsState 的值
        when {
            examViewModel != null && directExamNote != null -> {
                
                directExamNote
            }
            practiceViewModel != null && directPracticeNote != null -> {
                
                directPracticeNote
            }
            examViewModel != null && index < examNoteList.size -> {
                val content = examNoteList[index]

                content
            }
            practiceViewModel != null && index < practiceNoteList.size -> {
                val content = practiceNoteList[index]

                content
            }
            else -> {
                
                ""
            }
        }
    }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getDeepSeekFontSize(context, Float.NaN)
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
            FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    
    // 将 editableText 与 remember 绑定 currentNote，确保每次 noteList 变化时 UI 及时刷新
    val initialText = if (text.trim() == "") "" else text
    val editableTextState = remember(currentNote) { mutableStateOf(TextFieldValue(currentNote.ifBlank { initialText })) }
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
                    navController?.navigate("deepseek_ask/$questionId/$index/$encoded")
                }
            },
            aiServiceName = "DeepSeek"
        )
    }

    BackHandler {
        val currentDisplayText = currentNote.ifBlank { initialText }
        if (editableText.text != currentDisplayText) {
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
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)) {
                    BasicTextField(
                        value = editableText,
                        onValueChange = { editableText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
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
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.settings))
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_font)) }, onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_font)) }, onClick = {
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
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text(stringResource(R.string.save)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text(stringResource(R.string.cancel)) }
                },
                text = { Text(stringResource(R.string.confirm_save_changes)) }
            )
        }
    }
}
