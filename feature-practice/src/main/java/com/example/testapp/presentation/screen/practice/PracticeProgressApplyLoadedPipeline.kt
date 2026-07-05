package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

/** loadQuestions Ready 结果 → session / catalog / map 更新（纯逻辑） */
object PracticeProgressApplyLoadedPipeline {

    data class LoadQuestionsLogContext(
        val priorComplete: Boolean,
        val startNewRound: Boolean,
        val canReuse: Boolean,
        val savedSize: Int,
        val questionCount: Int,
        val orderedIds: List<Int>,
        val answeredSourceIds: Set<Int>,
        val lastRoundSourceIds: Set<Int>,
        val savedSourcesDone: Boolean,
        val restoreFromMap: Boolean,
        val restoredMapSize: Int,
    )

    data class AppliedLoaded(
        val sourceCatalogQuestions: List<Question>,
        val cumulativeQuestionStateMap: Map<Int, UnifiedQuestionState>,
        val questionsWithState: List<QuestionWithState>,
        val sessionStartTime: Long,
        val questionCount: Int,
        val startIndex: Int,
        val initKey: String,
        val restoreFromMap: Boolean,
        val logContext: LoadQuestionsLogContext,
    )

    fun apply(
        loaded: PracticeProgressLoadQuestionsPipeline.Loaded,
        existingProgress: PracticeProgress?,
    ): AppliedLoaded {
        val answeredSourceIds = PracticeSourceQuestionPipeline.answeredSourceIds(
            existingProgress?.questionStateMap.orEmpty(),
        )
        val lastRoundSourceIds =
            if (loaded.roundContext.startNewRound) {
                PracticeSourceQuestionPipeline.lastRoundSourceIds(
                    existingProgress?.fixedQuestionOrder.orEmpty(),
                )
            } else {
                emptySet()
            }
        return AppliedLoaded(
            sourceCatalogQuestions = loaded.sourceCatalogQuestions,
            cumulativeQuestionStateMap = loaded.cumulativeQuestionStateMap,
            questionsWithState = loaded.questionsWithState,
            sessionStartTime = loaded.sessionStartTime,
            questionCount = loaded.questionCount,
            startIndex = loaded.startIndex,
            initKey = loaded.initKey,
            restoreFromMap = loaded.restoreFromMap,
            logContext =
                LoadQuestionsLogContext(
                    priorComplete = loaded.roundContext.priorComplete,
                    startNewRound = loaded.roundContext.startNewRound,
                    canReuse = loaded.roundContext.canReuseSavedOrder,
                    savedSize = loaded.roundContext.savedSourceOrder.size,
                    questionCount = loaded.questionCount,
                    orderedIds = loaded.questionsWithState.map { it.question.id },
                    answeredSourceIds = answeredSourceIds,
                    lastRoundSourceIds = lastRoundSourceIds,
                    savedSourcesDone = loaded.roundContext.savedSourcesDone,
                    restoreFromMap = loaded.restoreFromMap,
                    restoredMapSize = existingProgress?.questionStateMap?.size ?: 0,
                ),
        )
    }

    fun patchSessionState(
        current: PracticeSessionState,
        applied: AppliedLoaded,
    ): PracticeSessionState =
        current.copy(
            questionsWithState = applied.questionsWithState,
            sessionStartTime = applied.sessionStartTime,
            questionCount = applied.questionCount,
            currentIndex = applied.startIndex,
        )
}
