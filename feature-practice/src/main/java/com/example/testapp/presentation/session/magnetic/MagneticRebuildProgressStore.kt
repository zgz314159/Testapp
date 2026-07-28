package com.example.testapp.presentation.session.magnetic

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.usecase.ProgressUseCases
import kotlinx.coroutines.flow.first

internal data class MagneticRebuildSavedProgress(
    val fixedQuestionOrder: List<Int>,
    val currentClauseIndex: Int,
    val completedQuestionIds: List<Int>,
    val currentQuestionId: Int?,
    val candidateTokenIds: List<Int>,
    val placedTokenIds: List<Int>,
    val moveCount: Int,
    val wrongCheckCount: Int,
    val hintCount: Int,
    val originalViewCount: Int,
    val hintedTokenId: Int?,
    val currentCompleted: Boolean,
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
    private const val SESSION_FORMAT = "magnetic-rebuild-v1"
    private const val COUNTER_SEPARATOR = ","

    fun encode(
        bankId: String,
        saved: MagneticRebuildSavedProgress,
    ): PracticeProgress =
        PracticeProgress(
            id = "magnetic_$bankId",
            currentIndex = saved.currentClauseIndex,
            answeredList = saved.completedQuestionIds,
            selectedOptions = listOf(saved.candidateTokenIds, saved.placedTokenIds),
            showResultList = listOf(saved.currentCompleted),
            analysisList =
                listOf(
                    saved.currentQuestionId?.toString().orEmpty(),
                    saved.hintedTokenId?.toString().orEmpty(),
                ),
            noteList =
                listOf(
                    listOf(
                        saved.moveCount,
                        saved.wrongCheckCount,
                        saved.hintCount,
                        saved.originalViewCount,
                    ).joinToString(COUNTER_SEPARATOR),
                ),
            timestamp = System.currentTimeMillis(),
            sessionId = SESSION_FORMAT,
            fixedQuestionOrder = saved.fixedQuestionOrder,
        )

    fun decode(progress: PracticeProgress): MagneticRebuildSavedProgress? {
        if (progress.sessionId != SESSION_FORMAT || progress.fixedQuestionOrder.isEmpty()) return null
        val counters =
            progress.noteList
                .firstOrNull()
                .orEmpty()
                .split(COUNTER_SEPARATOR)
                .map { it.toIntOrNull() ?: 0 }
        return MagneticRebuildSavedProgress(
            fixedQuestionOrder = progress.fixedQuestionOrder,
            currentClauseIndex = progress.currentIndex,
            completedQuestionIds = progress.answeredList,
            currentQuestionId = progress.analysisList.getOrNull(0)?.toIntOrNull(),
            candidateTokenIds = progress.selectedOptions.getOrNull(0).orEmpty(),
            placedTokenIds = progress.selectedOptions.getOrNull(1).orEmpty(),
            moveCount = counters.getOrElse(0) { 0 }.coerceAtLeast(0),
            wrongCheckCount = counters.getOrElse(1) { 0 }.coerceAtLeast(0),
            hintCount = counters.getOrElse(2) { 0 }.coerceAtLeast(0),
            originalViewCount = counters.getOrElse(3) { 0 }.coerceAtLeast(0),
            hintedTokenId = progress.analysisList.getOrNull(1)?.toIntOrNull(),
            currentCompleted = progress.showResultList.firstOrNull() == true,
        )
    }
}
