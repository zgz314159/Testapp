package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.core.util.transformQuestionVariantsForFillSettings
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlinx.coroutines.flow.firstOrNull

data class ExamFillConfig(
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

    val isFullAnswerMode: Boolean
        get() = generationMode == FillQuestionGenerationMode.FULL_ANSWER

    companion object {
        val default = ExamFillConfig(
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

fun ExamFillConfig.forExamSession(): ExamFillConfig =
    copy(fullAnswerRequireCorrect = false)

object ExamFillConfigPipeline {
    suspend fun read(fontSettings: FontSettingsRepository): ExamFillConfig = ExamFillConfig(
        maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0,
        generationMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
        fullAnswerRandomOrder = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true,
        fullAnswerRequireCorrect = fontSettings.fillFullAnswerRequireCorrect.firstOrNull() ?: false,
        minAnswerScore = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1,
        maxAnswerScore = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10,
        answerTagFilter = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
    )

    suspend fun readForExam(fontSettings: FontSettingsRepository): ExamFillConfig =
        read(fontSettings).forExamSession()

    fun isFillConfigSensitive(questions: List<Question>): Boolean {
        if (questions.isEmpty() || !questions.all { QuestionTypes.isInlineBlank(it.type) }) return false
        return questions.any { question ->
            splitFillAnswerDescriptors(question.answer).any { descriptor ->
                descriptor.score != null || !descriptor.category.isNullOrBlank()
            }
        }
    }

    fun applyTransform(
        questions: List<Question>,
        config: ExamFillConfig,
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
