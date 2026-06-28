package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState

object PracticeProgressMapPipeline {
    fun mergeInto(
        existing: Map<Int, UnifiedQuestionState>,
        questionsWithState: List<QuestionWithState>
    ): Map<Int, UnifiedQuestionState> {
        val merged = existing.toMutableMap()
        questionsWithState.forEach { qws ->
            merged[qws.question.id] = UnifiedQuestionState(
                questionId = qws.question.id,
                selectedOptions = qws.selectedOptions,
                textAnswer = qws.textAnswer,
                showResult = qws.showResult,
                analysis = qws.analysis,
                sparkAnalysis = qws.sparkAnalysis,
                baiduAnalysis = qws.baiduAnalysis,
                note = qws.note,
                answerTime = qws.sessionAnswerTime
            )
        }
        return merged
    }
}
