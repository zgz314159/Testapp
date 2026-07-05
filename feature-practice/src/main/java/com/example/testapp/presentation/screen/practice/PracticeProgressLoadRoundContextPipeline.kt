package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question

data class PracticeProgressLoadRoundContext(
    val existingProgress: PracticeProgress?,
    val canReuseByFill: Boolean,
    val curFillSignature: String,
    val savedSourceOrder: List<Int>,
    val fullPoolSize: Int,
    val priorComplete: Boolean,
    val savedSourcesDone: Boolean,
    val startNewRound: Boolean,
    val canReuseSavedOrder: Boolean,
    val fillSeed: Long,
)

/** loadQuestionsForCurrentSource 轮次复用 / 新轮判定（纯逻辑） */
object PracticeProgressLoadRoundContextPipeline {

    data class FillSignatureUpgrade(
        val progress: PracticeProgress,
    )

    fun maybeUpgradeFillSignature(
        existingProgress: PracticeProgress?,
        canReuseByFill: Boolean,
        curFillSignature: String,
        progressId: String,
        newSessionStartTime: Long,
        progressCoordinator: PracticeProgressCoordinator,
    ): FillSignatureUpgrade? {
        if (existingProgress == null) return null
        val savedFillSignature = progressCoordinator.extractFillConfigSignature(existingProgress.sessionId)
        if (!savedFillSignature.isNullOrBlank() || !canReuseByFill) return null
        return FillSignatureUpgrade(
            progress = existingProgress.copy(
                sessionId = progressCoordinator.buildSessionIdWithFillSignature(
                    progressId,
                    progressCoordinator.practiceProgressSeed(existingProgress, newSessionStartTime),
                    curFillSignature,
                ),
            ),
        )
    }

    fun build(
        existingProgress: PracticeProgress?,
        originalQuestions: List<Question>,
        questionCount: Int,
        canReuseByFill: Boolean,
        curFillSignature: String,
        newSessionStartTime: Long,
        progressCoordinator: PracticeProgressCoordinator,
    ): PracticeProgressLoadRoundContext {
        val savedSourceOrder = existingProgress?.fixedQuestionOrder
            ?.map(::extractSourceQuestionId)
            ?.distinct()
            ?.takeIf { canReuseByFill && it.isNotEmpty() }
            .orEmpty()
        val fullPoolSize = originalQuestions.distinctBy { it.id }.size
        val priorComplete = PracticeRoundCompletePipeline.isComplete(existingProgress)
        val savedSourcesDone = PracticeSourceQuestionPipeline.savedSourcesFullyAnswered(
            savedSourceOrder,
            existingProgress?.questionStateMap.orEmpty(),
        )
        val startNewRound = priorComplete || savedSourcesDone
        val canReuseSavedOrder = PracticeRoundReusePipeline.canReuseSavedOrder(
            progress = existingProgress,
            savedSourceOrder = savedSourceOrder,
            questionCount = questionCount,
            fullPoolSize = fullPoolSize,
            canReuseByFill = canReuseByFill,
        )
        val fillSeed = progressCoordinator.practiceProgressSeed(
            if (canReuseByFill) existingProgress else null,
            newSessionStartTime,
        )
        return PracticeProgressLoadRoundContext(
            existingProgress = existingProgress,
            canReuseByFill = canReuseByFill,
            curFillSignature = curFillSignature,
            savedSourceOrder = savedSourceOrder,
            fullPoolSize = fullPoolSize,
            priorComplete = priorComplete,
            savedSourcesDone = savedSourcesDone,
            startNewRound = startNewRound,
            canReuseSavedOrder = canReuseSavedOrder,
            fillSeed = fillSeed,
        )
    }

    fun shouldWriteNewRoundProgress(
        context: PracticeProgressLoadRoundContext,
    ): Boolean =
        context.startNewRound ||
            context.existingProgress?.fixedQuestionOrder.isNullOrEmpty() ||
            !context.canReuseByFill ||
            !context.canReuseSavedOrder
}
