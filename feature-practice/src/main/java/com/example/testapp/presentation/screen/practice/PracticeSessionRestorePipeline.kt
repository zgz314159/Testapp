package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.QuestionWithState

/** 练习进度恢复：新轮次不回填作答 */
object PracticeSessionRestorePipeline {

    fun shouldRestoreAnswersFromMap(progress: PracticeProgress?, reviewMode: Boolean = false): Boolean {
        if (progress == null) return false
        if (reviewMode) {
            return progress.questionStateMap.isNotEmpty() || progress.selectedOptions.any { it.isNotEmpty() }
        }
        if (PracticeRoundCompletePipeline.isComplete(progress)) return false
        val savedSources = progress.fixedQuestionOrder.map(::extractSourceQuestionId).distinct()
        if (PracticeSourceQuestionPipeline.savedSourcesFullyAnswered(savedSources, progress.questionStateMap)) {
            return false
        }
        if (progress.selectedOptions.isEmpty()) return false
        return progress.selectedOptions.any { it.isNotEmpty() }
    }

    fun resolveSessionQuestions(
        sessionQuestions: List<QuestionWithState>,
        progress: PracticeProgress?,
        restoreFromMap: Boolean,
        sessionStartTime: Long
    ): List<QuestionWithState> {
        if (!restoreFromMap || progress == null || progress.questionStateMap.isEmpty()) {
            return sessionQuestions
        }
        val total = sessionQuestions.size
        return sessionQuestions.mapIndexed { index, qw ->
            progress.questionStateMap[qw.question.id]?.let { saved ->
                PracticeProgressRestorePipeline.mergeQuestionState(
                    qws = qw,
                    savedState = saved,
                    sessionStartTime = sessionStartTime,
                    index = index,
                    totalCount = total
                )
            } ?: qw
        }
    }
}
