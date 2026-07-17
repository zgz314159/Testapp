package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** loadQuestionsForCurrentSource 题目加载 / 轮次 / 恢复计划（纯编排，IO 由 Coordinator 执行） */
object PracticeProgressLoadQuestionsPipeline {

    data class EmptyCatalog(
        val progressId: String,
        val sessionStartTime: Long,
    )

    data class Loaded(
        val sourceCatalogQuestions: List<Question>,
        val fillSignatureUpgrade: PracticeProgress?,
        val newRoundProgress: PracticeProgress?,
        val finalProgress: PracticeProgress?,
        val cumulativeQuestionStateMap: Map<Int, UnifiedQuestionState>,
        val questionsWithState: List<QuestionWithState>,
        val startIndex: Int,
        val questionCount: Int,
        val sessionStartTime: Long,
        val restoreFromMap: Boolean,
        val initKey: String,
        val roundContext: PracticeProgressLoadRoundContext,
    )

    sealed interface Outcome {
        data class Empty(val catalog: EmptyCatalog) : Outcome
        data class Ready(val loaded: Loaded) : Outcome
    }

    suspend fun prepare(
        originalQuestions: List<Question>,
        existingProgress: PracticeProgress?,
        questionCount: Int,
        newSessionStartTime: Long,
        fillConfig: PracticeFillConfig,
        sourceId: String,
        randomPracticeEnabled: Boolean,
        pinnedQuestionId: Int?,
        preserveCurrentIndex: Int?,
        progressCoordinator: PracticeProgressCoordinator,
        persistenceConfig: SessionPersistenceConfig,
        progressId: String,
        skipFillTransform: Boolean = false,
    ): Outcome {
        if (originalQuestions.isEmpty()) {
            return Outcome.Empty(
                EmptyCatalog(progressId = progressId, sessionStartTime = newSessionStartTime),
            )
        }

        val sourceCatalog = originalQuestions.distinctBy { it.id }
        val curFillSignature = fillConfig.signature()
        val fillConfigSensitive = PracticeFillConfigPipeline.isFillConfigSensitive(originalQuestions, sourceId)
        var progress = existingProgress.takeIf { persistenceConfig.restoreProgress }
        val canReuseByFill = progressCoordinator.canReuseByFillSignature(
            progress?.sessionId,
            curFillSignature,
            fillConfigSensitive,
        )

        val fillSignatureUpgrade =
            PracticeProgressLoadRoundContextPipeline.maybeUpgradeFillSignature(
                existingProgress = progress,
                canReuseByFill = canReuseByFill,
                curFillSignature = curFillSignature,
                progressId = progressId,
                newSessionStartTime = newSessionStartTime,
                progressCoordinator = progressCoordinator,
            )?.progress
        if (fillSignatureUpgrade != null) {
            progress = fillSignatureUpgrade
        }

        val roundContext = PracticeProgressLoadRoundContextPipeline.build(
            existingProgress = progress,
            originalQuestions = originalQuestions,
            questionCount = questionCount,
            canReuseByFill = canReuseByFill,
            curFillSignature = curFillSignature,
            newSessionStartTime = newSessionStartTime,
            progressCoordinator = progressCoordinator,
        )

        val orderedSourceQuestions = PracticeProgressLoadOrderPipeline.resolveOrderedSourceQuestions(
            originalQuestions = originalQuestions,
            context = roundContext,
            randomPracticeEnabled = randomPracticeEnabled,
            questionCount = questionCount,
            newSessionStartTime = newSessionStartTime,
        )
        val fillTransformed =
            if (skipFillTransform) {
                orderedSourceQuestions
            } else {
                withContext(Dispatchers.Default) {
                    PracticeFillConfigPipeline.applyTransform(orderedSourceQuestions, fillConfig, roundContext.fillSeed)
                }
            }
        val questionsWithFixedOrder =
            PracticePinnedQuestionPipeline.ensurePinned(
                ordered =
                    PracticeQuestionCountPolicy.limitQuestions(
                        fillTransformed,
                        questionCount,
                    ),
                catalog = sourceCatalog,
                pinnedQuestionId = pinnedQuestionId,
                questionCount = questionCount,
            )

        val newRoundProgress =
            if (
                persistenceConfig.persistProgress &&
                PracticeProgressLoadRoundContextPipeline.shouldWriteNewRoundProgress(roundContext)
            ) {
                PracticeNewRoundProgressPipeline.build(
                    prior = progress,
                    progressId = progressId,
                    seed = newSessionStartTime,
                    sessionId = progressCoordinator.buildSessionIdWithFillSignature(
                        progressId,
                        roundContext.fillSeed,
                        curFillSignature,
                    ),
                    questions = questionsWithFixedOrder,
                )
            } else {
                null
            }
        if (newRoundProgress != null) {
            progress = newRoundProgress
        }

        val restoreFromMap =
            PracticeProgressPersistencePipeline.shouldRestoreAnswersFromMap(persistenceConfig, progress)
        val baseQuestionsWithState = questionsWithFixedOrder.map { QuestionWithState(question = it) }
        val questionsWithState = PracticeSessionRestorePipeline.resolveSessionQuestions(
            sessionQuestions = baseQuestionsWithState,
            progress = progress,
            restoreFromMap = restoreFromMap,
            sessionStartTime = newSessionStartTime,
        )
        val startIndex = PracticeSessionStartIndexPipeline.resolve(
            questionCount = questionsWithState.size,
            restoreFromMap = restoreFromMap,
            savedCurrentIndex = progress?.currentIndex,
            randomPracticeEnabled = randomPracticeEnabled,
            sessionStartTime = newSessionStartTime,
            preserveCurrentIndex = preserveCurrentIndex,
        )
        val initKey = PracticeQuizInitReloadPipeline.buildInitKey(
            fillConfigVersion = curFillSignature,
            practiceCount = questionCount,
            randomPractice = randomPracticeEnabled,
        )

        return Outcome.Ready(
            Loaded(
                sourceCatalogQuestions = sourceCatalog,
                fillSignatureUpgrade = fillSignatureUpgrade,
                newRoundProgress = newRoundProgress,
                finalProgress = progress,
                cumulativeQuestionStateMap = progress?.questionStateMap.orEmpty(),
                questionsWithState = questionsWithState,
                startIndex = startIndex,
                questionCount = questionCount,
                sessionStartTime = newSessionStartTime,
                restoreFromMap = restoreFromMap,
                initKey = initKey,
                roundContext = roundContext,
            ),
        )
    }
}
