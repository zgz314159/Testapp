package com.example.testapp.presentation.screen.practice.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.testapp.core.session.strategy.exit.SessionExitGate
import com.example.testapp.core.util.SessionAnalysisResolvePipeline
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeCurrentQuestionUi
import com.example.testapp.presentation.screen.practice.PracticePostAnswerAdvancePipeline
import com.example.testapp.presentation.screen.practice.PracticeSessionExitPipeline
import com.example.testapp.presentation.session.practice.PracticeScreenBindings

data class PracticeScreenAnalysisTexts(
    val analysisText: String?,
    val sparkText: String?,
    val baiduText: String?,
    val hasDeepSeekAnalysis: Boolean,
    val hasSparkAnalysis: Boolean,
    val hasBaiduAnalysis: Boolean,
)

@Composable
fun rememberPracticeScreenAnalysisTexts(
    currentIndex: Int,
    currentQuestionUi: PracticeCurrentQuestionUi?,
    analysisPair: Pair<Int, String>?,
    sparkPair: Pair<Int, String>?,
    baiduPair: Pair<Int, String>?,
    analysisList: List<String>,
    sparkAnalysisList: List<String>,
    baiduAnalysisList: List<String>,
    parsingKeyword: String,
): PracticeScreenAnalysisTexts =
    remember(
        currentIndex,
        currentQuestionUi,
        analysisPair,
        sparkPair,
        baiduPair,
        analysisList,
        sparkAnalysisList,
        baiduAnalysisList,
        parsingKeyword,
    ) {
        val analysisText =
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = currentIndex,
                streamingPair = analysisPair,
                sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi.analysis else null,
                listValue = analysisList.getOrNull(currentIndex),
                parsingKeyword = parsingKeyword,
            )
        val sparkText =
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = currentIndex,
                streamingPair = sparkPair,
                sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi.sparkAnalysis else null,
                listValue = sparkAnalysisList.getOrNull(currentIndex),
                parsingKeyword = parsingKeyword,
            )
        val baiduText =
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = currentIndex,
                streamingPair = baiduPair,
                sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi.baiduAnalysis else null,
                listValue = baiduAnalysisList.getOrNull(currentIndex),
                parsingKeyword = parsingKeyword,
            )
        PracticeScreenAnalysisTexts(
            analysisText = analysisText,
            sparkText = sparkText,
            baiduText = baiduText,
            hasDeepSeekAnalysis = !analysisText.isNullOrBlank(),
            hasSparkAnalysis = !sparkText.isNullOrBlank(),
            hasBaiduAnalysis = !baiduText.isNullOrBlank(),
        )
    }

data class PracticeSessionExitHandle(
    val exitLabel: String,
    val requestExit: () -> Unit,
)

@Composable
fun rememberPracticeSessionExit(
    bindings: PracticeScreenBindings,
    answeredThisSession: Boolean,
    hasSessionInput: Boolean,
    sessionAnsweredCount: Int,
    sessionScore: Int,
    autoAdvance: PracticeAutoAdvanceController,
    onReviewBack: () -> Unit,
    onExitWithoutAnswer: () -> Unit,
    onQuizEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?) -> Unit,
    sendCommand: (SessionCommand) -> Unit,
    onShowExitDialog: () -> Unit,
): PracticeSessionExitHandle {
    val exitLabel =
        if (SessionExitGate.isReviewBackExit(bindings.exitConfig())) {
            stringResource(R.string.review_back)
        } else {
            stringResource(R.string.practice_exit_session)
        }
    val requestExit: () -> Unit =
        remember(
            bindings,
            answeredThisSession,
            hasSessionInput,
            sessionAnsweredCount,
            sessionScore,
            onReviewBack,
            onExitWithoutAnswer,
            onQuizEnd,
            sendCommand,
            onShowExitDialog,
        ) {
            {
                autoAdvance.cancel()
                when (
                    val action =
                        PracticeSessionExitPipeline.resolve(
                            isReviewMode = SessionExitGate.isReviewBackExit(bindings.exitConfig()),
                            answeredThisSession = answeredThisSession,
                            hasSessionInput = hasSessionInput,
                            sessionAnsweredCount = sessionAnsweredCount,
                            totalCount = bindings.totalCount,
                            sessionScore = sessionScore,
                            realUnanswered = bindings.totalCount - bindings.answeredCount,
                        )
                ) {
                    PracticeSessionExitPipeline.Action.ReviewBack -> {
                        sendCommand(SessionCommand.LeaveReviewSession)
                        onReviewBack()
                    }
                    PracticeSessionExitPipeline.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
                    is PracticeSessionExitPipeline.Action.FinishDirect -> {
                        if (sessionAnsweredCount > 0) {
                            sendCommand(
                                SessionCommand.AddHistoryRecord(
                                    sessionScore,
                                    bindings.totalCount,
                                    action.realUnanswered,
                                ),
                            )
                        }
                        onQuizEnd(
                            action.sessionScore,
                            action.sessionAnsweredCount,
                            action.realUnanswered,
                            bindings.correctCount,
                            bindings.answeredCount,
                        )
                    }
                    PracticeSessionExitPipeline.Action.ShowSubmitDialog -> onShowExitDialog()
                }
            }
        }
    return PracticeSessionExitHandle(exitLabel = exitLabel, requestExit = requestExit)
}

@Composable
fun rememberPracticePostAnswerAdvance(
    bindings: PracticeScreenBindings,
    currentIndex: Int,
    sessionAnsweredCount: Int,
    sessionScore: Int,
    sendCommand: (SessionCommand) -> Unit,
    onQuizEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?) -> Unit,
    onShowExitDialog: () -> Unit,
): suspend () -> Unit =
    remember(
        bindings,
        currentIndex,
        sessionAnsweredCount,
        sessionScore,
        sendCommand,
        onQuizEnd,
        onShowExitDialog,
    ) {
        {
            when (val action = PracticePostAnswerAdvancePipeline.resolve(bindings.hasPendingQuestions())) {
                PracticePostAnswerAdvancePipeline.Action.Advance -> {
                    sendCommand(SessionCommand.NextQuestion)
                }
                PracticePostAnswerAdvancePipeline.Action.FinishOrPromptExit -> {
                    if (sessionAnsweredCount >= bindings.totalCount) {
                        sendCommand(
                            SessionCommand.AddHistoryRecord(
                                sessionScore,
                                bindings.totalCount,
                                bindings.totalCount - bindings.answeredCount,
                            ),
                        )
                        onQuizEnd(
                            sessionScore,
                            sessionAnsweredCount,
                            bindings.totalCount - bindings.answeredCount,
                            bindings.correctCount,
                            bindings.answeredCount,
                        )
                    } else {
                        onShowExitDialog()
                    }
                }
            }
        }
    }
