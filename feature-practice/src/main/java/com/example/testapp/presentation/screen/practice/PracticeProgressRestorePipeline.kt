package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

/** 练习进度恢复：合并持久化字段并补偿缺失的作答时间。 */
object PracticeProgressRestorePipeline {

    fun mergeQuestionState(
        qws: QuestionWithState,
        savedState: UnifiedQuestionState?,
        sessionStartTime: Long,
        index: Int,
        totalCount: Int
    ): QuestionWithState {
        if (savedState == null) return qws
        val hasAnswerContent = savedState.textAnswer.isNotBlank() || savedState.selectedOptions.isNotEmpty()
        val isAnswered = savedState.showResult || hasAnswerContent
        val recoveredAnswerTime = when {
            !isAnswered -> 0L
            savedState.answerTime > 0L -> savedState.answerTime
            else -> fallbackAnswerTime(index, totalCount, sessionStartTime)
        }
        val showResult = when {
            savedState.showResult && hasAnswerContent -> true
            savedState.selectedOptions.isNotEmpty() ->
                savedState.showResult || (recoveredAnswerTime > 0L && recoveredAnswerTime < sessionStartTime)
            else -> savedState.showResult
        }
        return qws.copy(
            selectedOptions = savedState.selectedOptions,
            textAnswer = savedState.textAnswer,
            showResult = showResult,
            analysis = savedState.analysis.takeIf { it.isNotBlank() } ?: qws.analysis,
            sparkAnalysis = savedState.sparkAnalysis.takeIf { it.isNotBlank() } ?: qws.sparkAnalysis,
            baiduAnalysis = savedState.baiduAnalysis.takeIf { it.isNotBlank() } ?: qws.baiduAnalysis,
            note = savedState.note.takeIf { it.isNotBlank() } ?: qws.note,
            sessionAnswerTime = recoveredAnswerTime
        )
    }

    private fun fallbackAnswerTime(index: Int, total: Int, sessionStartTime: Long): Long {
        val offsetMs = (total - index).coerceAtLeast(1) * 1000L
        return (sessionStartTime - offsetMs).coerceAtLeast(1L)
    }
}
