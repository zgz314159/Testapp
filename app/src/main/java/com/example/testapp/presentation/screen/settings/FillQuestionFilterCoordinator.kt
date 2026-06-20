package com.example.testapp.presentation.screen.settings

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.buildFillQuestionFilterSummary
import com.example.testapp.core.util.splitFillAnswerDescriptors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillQuestionFilterCoordinator @Inject constructor() {

    fun refreshTags(questions: List<Question>): List<String> =
        questions.asSequence()
            .filter { QuestionTypes.isFill(it.type) }
            .flatMap { splitFillAnswerDescriptors(it.answer).asSequence() }
            .mapNotNull { it.category?.trim()?.takeIf(String::isNotBlank) }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }

    fun publish(
        questions: List<Question>,
        fillBlankCount: Int,
        generationMode: FillQuestionGenerationMode,
        fullAnswerRandomOrder: Boolean,
        minAnswerScore: Int,
        maxAnswerScore: Int,
        answerTagFilter: String
    ): FillQuestionFilterSummary = buildFillQuestionFilterSummary(
        questions = questions,
        maxVisibleBlanks = fillBlankCount,
        generationMode = generationMode,
        fullAnswerRandomOrder = fullAnswerRandomOrder,
        minAnswerScore = minAnswerScore,
        maxAnswerScore = maxAnswerScore,
        answerTagFilter = answerTagFilter
    )
}

