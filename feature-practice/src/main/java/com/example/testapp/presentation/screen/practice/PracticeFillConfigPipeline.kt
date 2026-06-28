package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.core.util.transformQuestionVariantsForFillSettings
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlinx.coroutines.flow.firstOrNull

data class PracticeFillConfig(
    val maxBlanks: Int,
    val generationMode: FillQuestionGenerationMode,
    val fullAnswerRandomOrder: Boolean,
    val fullAnswerRequireCorrect: Boolean,
    val minAnswerScore: Int,
    val maxAnswerScore: Int,
    val answerTagFilter: String
) {
    fun signature(): String = listOf(
        generationMode.storageValue,
        maxBlanks.toString(),
        fullAnswerRandomOrder.toString(),
        fullAnswerRequireCorrect.toString(),
        minAnswerScore.toString(),
        maxAnswerScore.toString(),
        answerTagFilter.trim()
    ).joinToString("|")

    companion object {
        val default = PracticeFillConfig(
            maxBlanks = 0,
            generationMode = FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
            fullAnswerRandomOrder = true,
            fullAnswerRequireCorrect = false,
            minAnswerScore = 1,
            maxAnswerScore = 10,
            answerTagFilter = ""
        )
    }
}

object PracticeFillConfigPipeline {
    suspend fun read(fontSettings: FontSettingsRepository): PracticeFillConfig = PracticeFillConfig(
        maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0,
        generationMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
        fullAnswerRandomOrder = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true,
        fullAnswerRequireCorrect = fontSettings.fillFullAnswerRequireCorrect.firstOrNull() ?: false,
        minAnswerScore = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1,
        maxAnswerScore = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10,
        answerTagFilter = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
    )

    fun isFillConfigSensitive(questions: List<Question>, sourceId: String): Boolean {
        if (questions.isEmpty()) return false
        if (!sourceId.endsWith(".json", ignoreCase = true) &&
            !sourceId.endsWith(".sqlite", ignoreCase = true) &&
            !sourceId.endsWith(".db", ignoreCase = true)
        ) {
            return false
        }
        if (!questions.all { QuestionTypes.isInlineBlank(it.type) }) return false
        return questions.any { question ->
            splitFillAnswerDescriptors(question.answer).any { descriptor ->
                descriptor.score != null || !descriptor.category.isNullOrBlank()
            }
        }
    }

    fun applyTransform(
        questions: List<Question>,
        config: PracticeFillConfig,
        seed: Long
    ): List<Question> = questions.flatMapIndexed { index, question ->
        if (QuestionTypes.isInlineBlank(question.type)) {
            transformQuestionVariantsForFillSettings(
                question = question,
                maxVisibleBlanks = config.maxBlanks,
                generationMode = config.generationMode,
                fullAnswerRandomOrder = config.fullAnswerRandomOrder,
                minAnswerScore = config.minAnswerScore,
                maxAnswerScore = config.maxAnswerScore,
                answerTagFilter = config.answerTagFilter,
                seed = seed + question.id.toLong() + index
            )
        } else {
            listOf(question)
        }
    }
}
