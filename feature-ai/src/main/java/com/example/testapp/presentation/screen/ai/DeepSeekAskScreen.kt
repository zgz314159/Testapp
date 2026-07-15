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
import com.example.testapp.data.network.deepseek.DeepSeekAskPersistDebugLog
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
    seedAnalysis: String? = null,
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

    var loadedQuestionId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(questionId, text, seedAnalysis) {
        DeepSeekAskPersistDebugLog.d(
            "UI.LaunchedEffect",
            "qId=$questionId routeIdx=$index loadedQ=$loadedQuestionId " +
                "seed.${DeepSeekAskPersistDebugLog.meta(seedAnalysis)} " +
                "seedPreview=${DeepSeekAskPersistDebugLog.preview(seedAnalysis)} " +
                "routeText=${DeepSeekAskPersistDebugLog.preview(text, 48)} " +
                "turnsBefore=${chatTurns.size}",
        )
        if (loadedQuestionId != questionId) {
            viewModel.reset()
            viewModel.setExamAnchor(examAnchor)
            loadedQuestionId = questionId
            inputText = text
        } else {
            viewModel.setExamAnchor(examAnchor)
        }
        val saved = viewModel.loadSaved(questionId, text, seedDisplay = seedAnalysis)
        DeepSeekAskPersistDebugLog.d(
            "UI.afterLoad",
            "qId=$questionId savedNull=${saved.isNullOrBlank()} " +
                "turnsAfter will recompose; display=${DeepSeekAskPersistDebugLog.preview(saved)}",
        )
        if (!saved.isNullOrBlank()) {
            inputText = ""
        } else if (loadedQuestionId == questionId && inputText.isBlank()) {
            inputText = text
        }
    }

    BackHandler {
        when {
            showSaveDialog -> showSaveDialog = false
            AiChatSaveGatePipeline.shouldConfirmSave(
                content = displayText,
                isParsing = isParsing,
                parsingKeyword = parsingKeyword,
                parseFailedKeyword = parseFailedKeyword,
            ) -> showSaveDialog = true
            else -> {
                AiAnalysisDebugLog.aiPopBack(index, saved = false)
                onBack()
            }
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
                decreaseFontLabel = decreaseFontText,
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
            assistantTextToolbar = toolbar,
            onAssistantContentChange = { messageIndex, value ->
                viewModel.updateAssistantByMessageIndex(messageIndex, value)
            },
        )
    }

    AiAskSaveConfirmDialog(
        visible = showSaveDialog,
        message = confirmSaveText,
        saveLabel = saveText,
        dismissLabel = dontSaveText,
        onSave = {
            saveScope.launch {
                DeepSeekAskPersistDebugLog.d(
                    "UI.saveClick",
                    "qId=$questionId routeIdx=$index turns=${chatTurns.size} " +
                        "display.${DeepSeekAskPersistDebugLog.meta(displayText)}",
                )
                val saved = viewModel.saveAndWait(questionId, displayText)
                DeepSeekAskPersistDebugLog.d(
                    "UI.saveResult",
                    "qId=$questionId ok=${saved != null} writeback.${DeepSeekAskPersistDebugLog.meta(saved)}",
                )
                if (saved != null) {
                    AiAnalysisDebugLog.analysisSave(index, null, questionId)
                    onSave(saved)
                    DeepSeekAskPersistDebugLog.d("UI.writeback.dispatched", "qId=$questionId routeIdx=$index")
                } else {
                    DeepSeekAskPersistDebugLog.w("UI.writeback.skipped", "qId=$questionId saved=null")
                }
                showSaveDialog = false
                AiAnalysisDebugLog.aiPopBack(index, saved = saved != null)
                onBack()
            }
        },
        onDiscardAndLeave = {
            showSaveDialog = false
            AiAnalysisDebugLog.aiPopBack(index, saved = false)
            onBack()
        },
        onCloseDialogOnly = { showSaveDialog = false },
    )
}
