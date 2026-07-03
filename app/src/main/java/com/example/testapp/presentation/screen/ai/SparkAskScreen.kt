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
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.uicommon.component.AiChatConversationLayout
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.design.AiChatSaveGatePipeline
import com.example.testapp.uicommon.design.AiChatSingleTurnPipeline
import com.example.testapp.uicommon.layout.ArtifactFullscreenShell
import kotlinx.coroutines.launch

@Composable
fun SparkAskScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: suspend (String) -> Unit = {},
    viewModel: SparkAskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
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
        .getSparkFontSize(context, Float.NaN)
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
            FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
        }
    }
    var inputText by remember { mutableStateOf(text) }
    var lastAskedQuestion by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }
    val isParsing = result == parsingText
    val view = LocalView.current
    val toolbar = remember(view) {
        ActionModeTextToolbar(
            view = view,
            onAIQuestion = { },
            aiServiceName = "Spark"
        )
    }
    val messages = remember(lastAskedQuestion, result, parsingText, parseFailedKeyword) {
        when {
            lastAskedQuestion.isBlank() && result.isBlank() -> emptyList()
            isParsing -> AiChatSingleTurnPipeline.build(lastAskedQuestion, "")
            result.contains(parseFailedKeyword) -> emptyList()
            else -> AiChatSingleTurnPipeline.build(lastAskedQuestion, result)
        }
    }
    val errorMessage = result.takeIf { it.contains(parseFailedKeyword) }

    LaunchedEffect(Unit) {
        viewModel.reset()
        inputText = text
        lastAskedQuestion = ""
        val saved = viewModel.getSavedNote(questionId)
        if (!saved.isNullOrBlank()) {
            lastAskedQuestion = text
            viewModel.restoreSaved(saved)
            inputText = ""
        }
    }

    BackHandler {
        if (AiChatSaveGatePipeline.shouldConfirmSave(
                content = result,
                isParsing = isParsing,
                parsingKeyword = parsingKeyword,
                parseFailedKeyword = parseFailedKeyword
            )
        ) {
            showSaveDialog = true
        } else {
            navController?.popBackStack()
        }
    }

    ArtifactFullscreenShell(
        topEndActions = {
            AiAskFontMenu(
                screenFontSize = screenFontSize,
                onFontSizeChange = { screenFontSize = it },
                fontSizeStore = FontSettingsDataStore::setSparkFontSize,
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
                lastAskedQuestion = draft
                viewModel.ask(draft)
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
                onSave(result)
                showSaveDialog = false
                navController?.popBackStack()
            }
        },
        onDismiss = {
            showSaveDialog = false
            navController?.popBackStack()
        }
    )
}
