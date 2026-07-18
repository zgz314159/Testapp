package com.example.testapp.data.repository

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.QuestionCorrectionApplySelection
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion

object QuestionCorrectionParsePipeline {

    fun needsOptions(questionType: String, currentOptions: List<String>): Boolean =
        QuestionTypes.isSingle(questionType) ||
            QuestionTypes.isMulti(questionType) ||
            QuestionTypes.isJudge(questionType) ||
            currentOptions.size >= 2

    fun validate(suggestion: QuestionCorrectionSuggestion, request: QuestionCorrectionRequest): QuestionCorrectionSuggestion {
        require(suggestion.content.isNotBlank()) { "纠正题干为空" }
        require(suggestion.answer.isNotBlank()) { "纠正答案为空" }
        require(suggestion.confidence in 0.0..1.0) { "置信度无效" }
        if (needsOptions(request.questionType, request.options)) {
            require(suggestion.options.size >= 2) { "选项数量不足" }
            require(suggestion.options.all { it.isNotBlank() }) { "存在空选项" }
        }
        return suggestion
    }

    fun applyToDraft(
        currentContent: String,
        currentOptions: List<String>,
        currentAnswer: String,
        currentExplanation: String,
        suggestion: QuestionCorrectionSuggestion,
        selection: QuestionCorrectionApplySelection,
    ): AppliedDraft = AppliedDraft(
        content = if (selection.applyContent) suggestion.content else currentContent,
        options = if (selection.applyOptions && suggestion.options.isNotEmpty()) {
            suggestion.options
        } else {
            currentOptions
        },
        answer = if (selection.applyAnswer) suggestion.answer else currentAnswer,
        explanation = if (selection.applyExplanation) {
            suggestion.explanation.ifBlank { currentExplanation }
        } else {
            currentExplanation
        },
    )

    data class AppliedDraft(
        val content: String,
        val options: List<String>,
        val answer: String,
        val explanation: String,
    )
}
