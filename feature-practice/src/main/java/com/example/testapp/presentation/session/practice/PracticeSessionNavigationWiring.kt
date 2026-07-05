package com.example.testapp.presentation.session.practice

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeAnswerHandler
import com.example.testapp.presentation.screen.practice.PracticeFillConfig
import com.example.testapp.presentation.screen.practice.PracticeNavigationCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

internal object PracticeSessionNavigationWiring {
    fun attach(
        navigationCoordinator: PracticeNavigationCoordinator,
        sessionState: MutableStateFlow<PracticeSessionState>,
        scope: CoroutineScope,
        answerHandler: PracticeAnswerHandler,
        activeFillConfig: () -> PracticeFillConfig,
        reopenQuestionForPendingRetry: (Int) -> Unit,
        reopenQuestionForFullAnswerRetry: (Int) -> Unit,
        scheduleNavigationSave: () -> Unit,
        randomPracticeEnabled: Boolean,
    ) {
        navigationCoordinator.randomPracticeEnabled = randomPracticeEnabled
        val fill = { activeFillConfig() }
        val fullAnswerActive = {
            fill().generationMode == FillQuestionGenerationMode.FULL_ANSWER
        }
        navigationCoordinator.initPhase4(
            _sessionState = sessionState,
            scope = scope,
            isQuestionPendingForCurrentMode = { qws ->
                answerHandler.isQuestionPendingForCurrentMode(
                    qws,
                    fullAnswerModeActive = fullAnswerActive(),
                    fullAnswerRequireCorrect = fill().fullAnswerRequireCorrect,
                )
            },
            isQuestionAnswered = answerHandler::isQuestionAnswered,
            shouldReopenUnansweredReveal = answerHandler::shouldReopenUnansweredReveal,
            currentSourcePendingIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.currentSourcePendingIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = fullAnswerActive(),
                    fullAnswerRequireCorrect = fill().fullAnswerRequireCorrect,
                )
            },
            isCurrentSourceComplete = { state ->
                answerHandler.isCurrentSourceComplete(
                    state,
                    fullAnswerModeActive = fullAnswerActive(),
                    fullAnswerRequireCorrect = fill().fullAnswerRequireCorrect,
                )
            },
            findNextSourceEntryIndices = { state ->
                answerHandler.findNextSourceEntryIndices(
                    state,
                    fullAnswerModeActive = fullAnswerActive(),
                    fullAnswerRequireCorrect = fill().fullAnswerRequireCorrect,
                )
            },
            findAdjacentDerivedQuestionIndex = { _, _ -> null },
            effectiveCurrentMemoryRoundQuestionIds = { emptySet() },
            nextFullAnswerCandidateIndices = { questionsWithState, currentIndex, eligibleIndices ->
                answerHandler.nextFullAnswerCandidateIndices(
                    questionsWithState,
                    currentIndex,
                    eligibleIndices,
                    fullAnswerModeActive = fullAnswerActive(),
                    fullAnswerRequireCorrect = fill().fullAnswerRequireCorrect,
                )
            },
            reopenQuestionForPendingRetry = reopenQuestionForPendingRetry,
            reopenQuestionForFullAnswerRetry = reopenQuestionForFullAnswerRetry,
            scheduleNavigationSave = scheduleNavigationSave,
            fullAnswerModeActive = fullAnswerActive,
            fullAnswerRequireCorrect = { fill().fullAnswerRequireCorrect },
            fullAnswerRandomOrder = { fill().fullAnswerRandomOrder },
            memoryModeActive = { false },
        )
    }
}
