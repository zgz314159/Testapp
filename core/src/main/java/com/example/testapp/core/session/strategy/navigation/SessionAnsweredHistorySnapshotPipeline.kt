package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.AnsweredBrowseOrder

/** 已答历史快照写入与解析（从 NavigationHistory 收编） */
object SessionAnsweredHistorySnapshotPipeline {
    fun shouldCapture(showResult: Boolean): Boolean = showResult

    fun shouldReplaceExisting(
        existingAnswerTime: Long?,
        candidateAnswerTime: Long,
    ): Boolean = existingAnswerTime == null || candidateAnswerTime >= existingAnswerTime

    fun resolveBrowsableSnapshot(
        live: QuestionWithState,
        storedSnapshot: QuestionWithState?,
    ): QuestionWithState? {
        if (live.sessionAnswerTime > 0L &&
            (live.showResult || AnsweredBrowseOrder.hasAnswerContent(live))
        ) {
            return live
        }
        return storedSnapshot
    }

    fun shouldKeepLiveStateOnApply(
        preferSnapshot: Boolean,
        liveShowResult: Boolean,
        isQuestionAnswered: Boolean,
    ): Boolean = !preferSnapshot && liveShowResult && isQuestionAnswered
}
