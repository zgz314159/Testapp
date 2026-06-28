package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.ExamProgress

/** 考试进度恢复：是否从 questionStateMap 回填作答态 */
object ExamSessionRestorePipeline {

    fun shouldRestoreAnswersFromStateMap(progress: ExamProgress, reviewMode: Boolean = false): Boolean {
        if (reviewMode) {
            return progress.questionStateMap.isNotEmpty() || progress.selectedOptions.any { it.isNotEmpty() }
        }
        if (ExamRoundCompletePipeline.isComplete(progress)) return false
        val savedSources = progress.fixedQuestionOrder.map(::extractSourceQuestionId).distinct()
        if (ExamSourceQuestionPipeline.savedSourcesFullyAnswered(savedSources, progress.questionStateMap)) {
            return false
        }
        if (progress.selectedOptions.isEmpty()) return false
        return progress.selectedOptions.any { it.isNotEmpty() }
    }

    fun resolveSessionQuestions(
        sessionQuestions: List<com.example.testapp.domain.model.QuestionWithState>,
        progress: ExamProgress,
        restoreFromMap: Boolean
    ): List<com.example.testapp.domain.model.QuestionWithState> {
        if (!restoreFromMap || progress.questionStateMap.isEmpty()) return sessionQuestions
        return sessionQuestions.map { qw ->
            progress.questionStateMap[qw.question.id]?.let { saved ->
                qw.copy(
                    selectedOptions = saved.selectedOptions,
                    textAnswer = saved.textAnswer,
                    showResult = saved.showResult,
                    analysis = saved.analysis,
                    sparkAnalysis = saved.sparkAnalysis,
                    baiduAnalysis = saved.baiduAnalysis,
                    note = saved.note.takeIf { it.isNotBlank() } ?: qw.note,
                    sessionAnswerTime = saved.answerTime
                )
            } ?: qw
        }
    }
}
