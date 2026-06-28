package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.transformQuestionForFillSettings
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamFillTransform @Inject constructor(
    private val fontSettings: FontSettingsRepository
) {
    suspend fun currentFillConfigSignature(): String =
        ExamFillConfigPipeline.read(fontSettings).signature()

    fun extractFillConfigSignature(sessionId: String?) =
        sessionId?.substringAfter("|fill=", "").orEmpty().takeIf { it.isNotBlank() }

    fun buildSessionIdWithFillSignature(baseId: String, seed: Long, sig: String) =
        "${baseId}_${seed}|fill=$sig"

    fun isFillConfigSensitive(questions: List<Question>): Boolean =
        ExamFillConfigPipeline.isFillConfigSensitive(questions)

    fun canReuseByFillSignature(sessionId: String?, currentSig: String, sensitive: Boolean): Boolean {
        val saved = extractFillConfigSignature(sessionId)
        return if (saved.isNullOrBlank()) !sensitive else saved == currentSig
    }

    suspend fun readConfig(): ExamFillConfig = ExamFillConfigPipeline.read(fontSettings)

    suspend fun applyConfiguredFillQuestions(
        questions: List<Question>,
        progressSeed: Long,
        onConfigApplied: (ExamFillConfig) -> Unit,
        onEmptyResult: (LocalizedResult?) -> Unit
    ): List<Question> {
        if (questions.isEmpty()) {
            onEmptyResult(null)
            return emptyList()
        }
        val config = ExamFillConfigPipeline.readForExam(fontSettings)
        onConfigApplied(config)
        val configured = ExamFillConfigPipeline.applyTransform(questions, config, progressSeed)
        onEmptyResult(if (configured.isEmpty()) LocalizedResult("fill_no_questions_after_filter") else null)
        return configured
    }

    suspend fun resolveFillDisplayedQuestion(
        sourceQuestion: Question,
        displayedQuestionId: Int,
        index: Int,
        progressSeed: Long
    ): Question {
        if (!QuestionTypes.isInlineBlank(sourceQuestion.type)) return sourceQuestion
        val config = ExamFillConfigPipeline.readForExam(fontSettings)
        return ExamFillConfigPipeline.applyTransform(listOf(sourceQuestion), config, progressSeed + index)
            .firstOrNull { it.id == displayedQuestionId }
            ?: transformQuestionForFillSettings(
                sourceQuestion,
                config.maxBlanks,
                config.generationMode,
                config.fullAnswerRandomOrder,
                config.minAnswerScore,
                config.maxAnswerScore,
                config.answerTagFilter,
                progressSeed + sourceQuestion.id.toLong() + index
            )
            ?: sourceQuestion
    }
}
