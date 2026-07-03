package com.example.testapp.presentation.screen.ai

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchor
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.uicommon.component.AiChatConversationLayout
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.design.AiChatSaveGatePipeline
import com.example.testapp.uicommon.design.AiChatTurnFlattenPipeline
import com.example.testapp.uicommon.layout.ArtifactFullscreenShell
import kotlinx.coroutines.launch

@Composable
fun DeepSeekAskScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: suspend (String) -> Unit = {},
    examAnchor: DeepSeekExamAnchor? = null,
    viewModel: DeepSeekAskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val displayText by viewModel.displayText.collectAsState()
    val chatTurns by viewModel.chatTurns.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val parsingText = stringResource(R.string.parsing)
    val parsingKeyword = parsingText.removeSuffix("...")
    val sendLabel = stringResource(R.string.ai_send)
    val inputPlaceholder = stringResource(R.string.ai_input_placeholder)
    val settingsText = stringResource(R.string.settings)
    val increaseFontText = stringResource(R.string.increase_font)
    val decreaseFontText = stringResource(R.string.decrease_font)
    val saveText = stringResource(R.string.save)
    val dontSaveText = stringResource(R.string.cancel)
    val confirmSaveText = stringResource(R.string.confirm_save_changes)
    val parseFailedKeyword = stringResource(R.string.parse_failed)
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val saveScope = rememberCoroutineScope()
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
    var inputText by remember { mutableStateOf(text) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view) {
        ActionModeTextToolbar(
            view = view,
            onAIQuestion = { },
            aiServiceName = "DeepSeek"
        )
    }
    val messages = remember(chatTurns) {
        AiChatTurnFlattenPipeline.flatten(DeepSeekAskChatTurnMapPipeline.map(chatTurns))
    }

    LaunchedEffect(Unit) {
        viewModel.reset()
        viewModel.setExamAnchor(examAnchor)
        inputText = text
        val saved = viewModel.loadSaved(questionId, text)
        if (!saved.isNullOrBlank()) {
            inputText = ""
        }
    }

    BackHandler {
        if (AiChatSaveGatePipeline.shouldConfirmSave(
                content = displayText,
                isParsing = isParsing,
                parsingKeyword = parsingKeyword,
                parseFailedKeyword = parseFailedKeyword
            )
        ) {
            showSaveDialog = true
        } else {
            PracticeJumpDebugLog.aiPopBack(index, saved = false)
            navController?.popBackStack()
        }
    }

    ArtifactFullscreenShell(
        topEndActions = {
            AiAskFontMenu(
                screenFontSize = screenFontSize,
                onFontSizeChange = { screenFontSize = it },
                fontSizeStore = FontSettingsDataStore::setDeepSeekFontSize,
                settingsLabel = settingsText,
                increaseFontLabel = increaseFontText,
                decreaseFontLabel = decreaseFontText
            )
        }
    ) { contentModifier ->
        AiChatConversationLayout(
            modifier = contentModifier,
            messages = messages,
            isTyping = isParsing,
            typingLabel = parsingText,
            errorMessage = errorMessage,
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                val draft = inputText.trim()
                if (draft.isEmpty() || isParsing) return@AiChatConversationLayout
                viewModel.ask(inputText)
                inputText = ""
            },
            sendEnabled = !isParsing,
            sendContentDescription = sendLabel,
            inputPlaceholder = inputPlaceholder,
            assistantFontSize = screenFontSize.sp,
            assistantFontFamily = LocalFontFamily.current,
            assistantTextToolbar = toolbar
        )
    }

    AiAskSaveConfirmDialog(
        visible = showSaveDialog,
        message = confirmSaveText,
        saveLabel = saveText,
        dismissLabel = dontSaveText,
        onSave = {
            saveScope.launch {
                val saved = viewModel.saveAndWait(questionId, displayText)
                if (saved != null) {
                    PracticeJumpDebugLog.analysisSave(index, null, questionId)
                    onSave(saved)
                }
                showSaveDialog = false
                PracticeJumpDebugLog.aiPopBack(index, saved = saved != null)
                navController?.popBackStack()
            }
        },
        onDismiss = {
            showSaveDialog = false
            PracticeJumpDebugLog.aiPopBack(index, saved = false)
            navController?.popBackStack()
        }
    )
}
