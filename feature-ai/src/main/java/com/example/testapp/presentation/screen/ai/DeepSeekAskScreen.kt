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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.network.deepseek.DeepSeekExamAnchor
import com.example.testapp.feature.ai.R
import com.example.testapp.uicommon.component.ActionModeTextToolbar
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
    onBack: () -> Unit = {},
    onSave: suspend (String) -> Unit = {},
    examAnchor: DeepSeekExamAnchor? = null,
    viewModel: DeepSeekAskViewModel = hiltViewModel(),
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
    val askMenuLabel = stringResource(R.string.ask)
    val fontState = rememberAiFontSize(AiFontScope.DEEPSEEK)
    val saveScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf(text) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view, askMenuLabel) {
        ActionModeTextToolbar(
            view = view,
            onAIQuestion = { },
            aiServiceName = "DeepSeek",
            askMenuLabel = askMenuLabel,
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
            AiAnalysisDebugLog.aiPopBack(index, saved = false)
            onBack()
        }
    }

    ArtifactFullscreenShell(
        topEndActions = {
            AiAskFontMenu(
                screenFontSize = fontState.size,
                onFontSizeChange = fontState.setSize,
                fontSizeStore = fontState.persistSize,
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
            assistantFontSize = fontState.size.sp,
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
                    AiAnalysisDebugLog.analysisSave(index, null, questionId)
                    onSave(saved)
                }
                showSaveDialog = false
                AiAnalysisDebugLog.aiPopBack(index, saved = saved != null)
                onBack()
            }
        },
        onDismiss = {
            showSaveDialog = false
            AiAnalysisDebugLog.aiPopBack(index, saved = false)
            onBack()
        }
    )
}
