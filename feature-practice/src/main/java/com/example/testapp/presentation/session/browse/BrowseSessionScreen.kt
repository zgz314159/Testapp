package com.example.testapp.presentation.session.browse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.util.answerToOptionIndices
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.practice.PracticeQuestionContent
import com.example.testapp.presentation.screen.practice.localizedQuestionTypeLabel
import com.example.testapp.uicommon.component.QuestionNavigationControls
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppLoadingContent
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.PracticeExamTopBar
import com.example.testapp.uicommon.design.QuestionSessionChromeLayout
import com.example.testapp.uicommon.design.QuestionSessionHeader
import com.example.testapp.uicommon.layout.ScreenSafeScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseSessionScreen(
    session: BrowseSession,
    questionFontSize: Float,
    lineSpacing: Float,
    letterSpacing: Float,
    onExit: () -> Unit,
) {
    val state by session.practiceState.collectAsState()
    val question = state.currentQuestion?.question

    BackHandler(onBack = onExit)

    ScreenSafeScaffold { contentModifier ->
        if (!state.progressLoaded) {
            AppLoadingContent(
                modifier = contentModifier,
                message = stringResource(R.string.no_questions_loading),
            )
            return@ScreenSafeScaffold
        }
        if (question == null) {
            AppEmptyState(
                message = stringResource(R.string.no_questions),
                modifier = contentModifier,
            )
            return@ScreenSafeScaffold
        }

        val scrollState = rememberScrollState()
        val qws = state.currentQuestion!!
        val displayOptions = resolveDisplayOptions(question)
        val resolvedFillAnswer = resolveFillCorrectAnswer(question)
        val correctIndices = answerToOptionIndices(question)

        QuestionSessionChromeLayout(
            scrollState = scrollState,
            onScrollInProgress = {},
            modifier =
                contentModifier
                    .fillMaxSize()
                    .padding(AppSpacing.md),
            topBar = {
                PracticeExamTopBar(
                    elapsedSeconds = 0,
                    onRequestExit = onExit,
                    exitContentDescription = stringResource(R.string.browse_back),
                    isFavorite = false,
                    favoriteAddLabel = "",
                    favoriteRemoveLabel = "",
                    notesLabel = "",
                    onEditNote = {},
                    hasNote = false,
                    aiParseLabel = "",
                    deepSeekLabel = "",
                    sparkLabel = "",
                    aiMenuExpanded = false,
                    onAiMenuToggle = {},
                    onAiMenuDismiss = {},
                    onOpenAiMenu = {},
                    onOpenAskMenu = {},
                    onToggleFavorite = {},
                    onOpenTypography = {},
                    onEditQuestion = {},
                    settingsLabel = "",
                    settingsMenuExpanded = false,
                    onMenuToggle = {},
                    onMenuDismiss = {},
                    hasAnyAnalysis = false,
                )
            },
            scrollContent = {
                QuestionSessionHeader(
                    questionTypeLabel =
                        stringResource(R.string.question_type_prefix) +
                            localizedQuestionTypeLabel(question.type),
                    currentIndex = state.currentIndex,
                    total = state.totalCount,
                    questionListLabel = stringResource(R.string.total_questions, state.totalCount),
                    onOpenQuestionList = {},
                ) {}
                PracticeQuestionContent(
                    question = question,
                    textAnswer = qws.textAnswer,
                    showResult = false,
                    selectedOption = qws.selectedOptions,
                    displayOptions = displayOptions,
                    resolvedFillAnswer = resolvedFillAnswer,
                    questionFontSize = questionFontSize,
                    questionLineSpacing = lineSpacing,
                    questionLetterSpacing = letterSpacing,
                    onAnswerChange = {},
                    onOptionClick = {},
                    submitCurrentAnswer = {},
                )
            },
            bottomBar = {
                QuestionNavigationControls(
                    visible = true,
                    onPrev = { session.handle(com.example.testapp.domain.session.SessionCommand.PrevQuestion) },
                    onNext = { session.handle(com.example.testapp.domain.session.SessionCommand.NextQuestion) },
                    onSubmit = null,
                    enabledPrev = session.canStepBack(),
                    enabledNext = session.canStepForward(),
                )
            },
        )
    }
}
