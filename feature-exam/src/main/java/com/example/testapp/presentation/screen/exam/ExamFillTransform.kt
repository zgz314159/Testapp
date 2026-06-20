package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.core.util.transformQuestionForFillSettings
import com.example.testapp.core.util.transformQuestionVariantsForFillSettings
import com.example.testapp.core.util.splitFillAnswerDescriptors
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamFillTransform @Inject constructor(
    private val fontSettings: FontSettingsRepository
) {
    suspend fun currentFillConfigSignature(): String {
        val maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0
        val genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM
        val far = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true
        val farc = fontSettings.fillFullAnswerRequireCorrect.firstOrNull() ?: false
        val minS = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1
        val maxS = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10
        val tag = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
        return listOf(genMode.storageValue, maxBlanks.toString(), far.toString(), farc.toString(), minS.toString(), maxS.toString(), tag.trim()).joinToString("|")
    }
    fun extractFillConfigSignature(sessionId: String?) =
        sessionId?.substringAfter("|fill=", "").orEmpty().takeIf { it.isNotBlank() }

    fun buildSessionIdWithFillSignature(baseId: String, seed: Long, sig: String) =
        "${baseId}_${seed}|fill=$sig"

    fun isFillConfigSensitive(questions: List<Question>): Boolean {
        if (questions.isEmpty() || !questions.all { QuestionTypes.isInlineBlank(it.type) }) return false
        return questions.any { q ->
            splitFillAnswerDescriptors(q.answer).any {
                it.score != null || !it.category.isNullOrBlank()
            }
        }
    }

    fun canReuseByFillSignature(sessionId: String?, currentSig: String, sensitive: Boolean): Boolean {
        val saved = extractFillConfigSignature(sessionId)
        return if (saved.isNullOrBlank()) !sensitive else saved == currentSig
    }

    suspend fun applyConfiguredFillQuestions(
        questions: List<Question>, progressSeed: Long,
        onFullAnswerRequireCorrect: (Boolean) -> Unit,
        onEmptyResult: (LocalizedResult?) -> Unit
    ): List<Question> {
        if (questions.isEmpty()) { onEmptyResult(null); return emptyList() }
        val maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0
        val genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM
        val far = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true
        onFullAnswerRequireCorrect(fontSettings.fillFullAnswerRequireCorrect.firstOrNull() ?: false)
        val minS = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1
        val maxS = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10
        val tag = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
        val configured = questions.flatMapIndexed { i, q ->
            if (QuestionTypes.isInlineBlank(q.type)) {
                transformQuestionVariantsForFillSettings(q, maxBlanks, genMode, far, minS, maxS, tag, progressSeed + q.id.toLong() + i)
            } else {
                listOf(q)
            }
        }
        onEmptyResult(if (configured.isEmpty()) LocalizedResult("fill_no_questions_after_filter") else null)
        return configured
    }

    suspend fun resolveFillDisplayedQuestion(
        sourceQuestion: Question, displayedQuestionId: Int, index: Int, progressSeed: Long
    ): Question {
        if (!QuestionTypes.isInlineBlank(sourceQuestion.type)) return sourceQuestion
        val maxBlanks = fontSettings.fillBlankCount.firstOrNull() ?: 0
        val genMode = fontSettings.fillQuestionGenerationMode.firstOrNull()
            ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM
        val far = fontSettings.fillFullAnswerRandomOrder.firstOrNull() ?: true
        val minS = fontSettings.fillAnswerScoreMin.firstOrNull() ?: 1
        val maxS = fontSettings.fillAnswerScoreMax.firstOrNull() ?: 10
        val tag = fontSettings.fillAnswerTagFilter.firstOrNull().orEmpty()
        return transformQuestionVariantsForFillSettings(
            sourceQuestion, maxBlanks, genMode, far, minS, maxS, tag, progressSeed + sourceQuestion.id.toLong() + index
        ).firstOrNull { it.id == displayedQuestionId }
            ?: transformQuestionForFillSettings(sourceQuestion, maxBlanks, genMode, far, minS, maxS, tag, progressSeed + sourceQuestion.id.toLong() + index)
            ?: sourceQuestion
    }
}
