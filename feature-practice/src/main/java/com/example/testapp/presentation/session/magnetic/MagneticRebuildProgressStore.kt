package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.usecase.ProgressUseCases
import kotlinx.coroutines.flow.first

internal data class MagneticSavedClauseDraft(
    val candidateTokenIds: List<Int>,
    val placedTokenIds: List<Int>,
    val moveCount: Int,
    val wrongCheckCount: Int,
    val hintCount: Int,
    val originalViewCount: Int,
    val hintedTokenId: Int?,
    val completed: Boolean,
)

internal data class MagneticRebuildSavedProgress(
    val fixedQuestionOrder: List<Int>,
    val currentClauseIndex: Int,
    val completedQuestionIds: List<Int>,
    val currentQuestionId: Int?,
    val drafts: Map<Int, MagneticSavedClauseDraft>,
    val fragmentationLevel: MagneticFragmentationLevel,
)

internal class MagneticRebuildProgressStore(
    private val progress: ProgressUseCases,
) {
    suspend fun load(bankId: String): MagneticRebuildSavedProgress? =
        progress.getFlow(progressId(bankId)).first()?.let(MagneticRebuildProgressCodec::decode)

    suspend fun save(
        bankId: String,
        saved: MagneticRebuildSavedProgress,
    ) {
        progress.save(MagneticRebuildProgressCodec.encode(bankId, saved))
    }

    suspend fun clear(bankId: String) {
        progress.clear(progressId(bankId))
    }

    private fun progressId(bankId: String): String = "$PROGRESS_PREFIX$bankId"

    private companion object {
        const val PROGRESS_PREFIX = "magnetic_"
    }
}

internal object MagneticRebuildProgressCodec {
    private const val SESSION_FORMAT_V1 = "magnetic-rebuild-v1"
    private const val SESSION_FORMAT_V2 = "magnetic-rebuild-v2"
    private const val SESSION_FORMAT_V3 = "magnetic-rebuild-v3"
    private const val SEPARATOR = ","

    fun encode(
        bankId: String,
        saved: MagneticRebuildSavedProgress,
    ): PracticeProgress {
        val currentDraft = saved.currentQuestionId?.let(saved.drafts::get)
        return PracticeProgress(
            id = "magnetic_$bankId",
            currentIndex = saved.currentClauseIndex,
            answeredList = saved.completedQuestionIds,
            selectedOptions =
                listOf(
                    currentDraft?.candidateTokenIds.orEmpty(),
                    currentDraft?.placedTokenIds.orEmpty(),
                ),
            showResultList = listOf(currentDraft?.completed == true),
            analysisList = listOf(saved.currentQuestionId?.toString().orEmpty()),
            noteList = listOf(saved.fragmentationLevel.storageValue.toString()),
            timestamp = System.currentTimeMillis(),
            sessionId = SESSION_FORMAT_V3,
            fixedQuestionOrder = saved.fixedQuestionOrder,
            questionStateMap = saved.drafts.mapValues { (questionId, draft) -> draft.toUnifiedState(questionId) },
        )
    }

    fun decode(progress: PracticeProgress): MagneticRebuildSavedProgress? {
        if (progress.fixedQuestionOrder.isEmpty()) return null
        return when (progress.sessionId) {
            SESSION_FORMAT_V3 -> decodeV2(progress)
            SESSION_FORMAT_V2 -> decodeV2(progress).withoutLegacyDrafts()
            SESSION_FORMAT_V1 -> decodeV1(progress).withoutLegacyDrafts()
            else -> null
        }
    }

    private fun MagneticRebuildSavedProgress.withoutLegacyDrafts(): MagneticRebuildSavedProgress =
        copy(drafts = emptyMap())

    private fun decodeV2(progress: PracticeProgress): MagneticRebuildSavedProgress {
        val currentQuestionId = progress.analysisList.firstOrNull()?.toIntOrNull()
        return MagneticRebuildSavedProgress(
            fixedQuestionOrder = progress.fixedQuestionOrder,
            currentClauseIndex = progress.currentIndex,
            completedQuestionIds = progress.answeredList,
            currentQuestionId = currentQuestionId,
            drafts = progress.questionStateMap.mapValues { (_, state) -> state.toMagneticDraft() },
            fragmentationLevel =
                MagneticFragmentationLevel.fromStorageValue(
                    progress.noteList.firstOrNull()?.toIntOrNull(),
                ),
        )
    }

    private fun decodeV1(progress: PracticeProgress): MagneticRebuildSavedProgress {
        val counters =
            progress.noteList
                .firstOrNull()
                .orEmpty()
                .split(SEPARATOR)
                .map { it.toIntOrNull() ?: 0 }
        val currentQuestionId = progress.analysisList.getOrNull(0)?.toIntOrNull()
        val legacyDraft =
            currentQuestionId?.let { questionId ->
                questionId to
                    MagneticSavedClauseDraft(
                        candidateTokenIds = progress.selectedOptions.getOrNull(0).orEmpty(),
                        placedTokenIds = progress.selectedOptions.getOrNull(1).orEmpty(),
                        moveCount = counters.getOrElse(0) { 0 },
                        wrongCheckCount = counters.getOrElse(1) { 0 },
                        hintCount = counters.getOrElse(2) { 0 },
                        originalViewCount = counters.getOrElse(3) { 0 },
                        hintedTokenId = progress.analysisList.getOrNull(1)?.toIntOrNull(),
                        completed = progress.showResultList.firstOrNull() == true,
                    )
            }
        return MagneticRebuildSavedProgress(
            fixedQuestionOrder = progress.fixedQuestionOrder,
            currentClauseIndex = progress.currentIndex,
            completedQuestionIds = progress.answeredList,
            currentQuestionId = currentQuestionId,
            drafts = if (legacyDraft != null) mapOf(legacyDraft) else emptyMap(),
            fragmentationLevel = MagneticFragmentationLevel.fromStorageValue(counters.getOrNull(4)),
        )
    }

    private fun MagneticSavedClauseDraft.toUnifiedState(questionId: Int): UnifiedQuestionState =
        UnifiedQuestionState(
            questionId = questionId,
            selectedOptions = placedTokenIds,
            textAnswer = candidateTokenIds.joinToString(SEPARATOR),
            showResult = completed,
            analysis =
                listOf(
                    moveCount,
                    wrongCheckCount,
                    hintCount,
                    originalViewCount,
                    hintedTokenId ?: -1,
                ).joinToString(SEPARATOR),
        )

    private fun UnifiedQuestionState.toMagneticDraft(): MagneticSavedClauseDraft {
        val counters = analysis.split(SEPARATOR).map { it.toIntOrNull() ?: 0 }
        return MagneticSavedClauseDraft(
            candidateTokenIds = textAnswer.split(SEPARATOR).mapNotNull { value -> value.toIntOrNull() },
            placedTokenIds = selectedOptions,
            moveCount = counters.getOrElse(0) { 0 },
            wrongCheckCount = counters.getOrElse(1) { 0 },
            hintCount = counters.getOrElse(2) { 0 },
            originalViewCount = counters.getOrElse(3) { 0 },
            hintedTokenId = counters.getOrElse(4) { -1 }.takeIf { it >= 0 },
            completed = showResult,
        )
    }
}
