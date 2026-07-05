package com.example.testapp.presentation.session.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.presentation.screen.exam.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/** ExamSessionEngine 协调器装配上下文 */
internal data class ExamSessionWireContext(
    val scope: CoroutineScope,
    val deps: ExamSessionDeps,
    val sessionState: MutableStateFlow<PracticeSessionState>,
    val strategyCoordinator: ExamSessionStrategyCoordinator,
    val runtime: ExamSessionRuntimeState,
    val reviewModeActive: MutableStateFlow<Boolean>,
    val cumulativeCorrect: MutableStateFlow<Int>,
    val cumulativeAnswered: MutableStateFlow<Int>,
    val cumulativeExamCount: MutableStateFlow<Int>,
    val messageResult: MutableStateFlow<LocalizedResult?>,
    val saveSuccess: MutableSharedFlow<Unit>,
    val editableQuestion: MutableStateFlow<Question?>,
    val persistentQuestionStateMap: MutableMap<Int, UnifiedQuestionState>,
    val editedQuestionSnapshotMap: MutableMap<Int, Question>,
    val buildExamQuestionState: (Int) -> UnifiedQuestionState,
    val fullAnswerModeActiveNow: () -> Boolean,
    val initializeMemoryModeIfNeeded: suspend (Long) -> Boolean,
    val applyConfiguredFillQuestions: suspend (List<Question>) -> List<Question>,
    val calculateCumulativeStats: () -> Unit,
    val incrementExamCount: () -> Unit,
    val saveProgress: () -> Unit,
    val saveProgressInternal: suspend () -> Unit,
    val scheduleNavigationSave: () -> Unit,
    val loadProgress: () -> Unit,
    val reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    val refreshMemoryRoundPoolIfNeeded: suspend (Int) -> Boolean,
    val advanceMemoryRoundIfNeeded: suspend () -> Boolean,
    val effectiveCurrentMemoryRoundQuestionIds: () -> Set<Int>,
    val currentFullAnswerCandidateIndices: (List<Int>) -> List<Int>,
    val navigateToRandomUnansweredOrAdvanceRound: suspend () -> Unit,
) {
    val progressId: () -> String get() = { runtime.progressId }
    val setProgressId: (String) -> Unit get() = { runtime.progressId = it }
    val progressSeed: () -> Long get() = { runtime.progressSeed }
    val setProgressSeed: (Long) -> Unit get() = { runtime.progressSeed = it }
    val randomExamEnabled: () -> Boolean get() = { runtime.randomExamEnabled }
    val memoryModeActive: () -> Boolean get() = { runtime.memory.active }
    val setMemoryModeActive: (Boolean) -> Unit get() = { runtime.memory.active = it }
    val memoryModeEnabled: () -> Boolean get() = { runtime.memory.enabled }
    val memoryModeBatchSize: () -> Int get() = { runtime.memory.batchSize }
    val memoryWrongMode: () -> Int get() = { runtime.memory.wrongMode }
    val memoryPoolMode: () -> Int get() = { runtime.memory.poolMode }
    val allSourceQuestions: () -> List<Question> get() = { runtime.allSourceQuestions }
    val setAllSourceQuestions: (List<Question>) -> Unit get() = { runtime.allSourceQuestions = it }
    val currentMemoryRoundQuestionIds: () -> Set<Int> get() = { runtime.currentMemoryRoundQuestionIds }
    val setCurrentMemoryRoundQuestionIds: (Set<Int>) -> Unit get() = { runtime.currentMemoryRoundQuestionIds = it }
    val fullAnswerRequireCorrect: () -> Boolean get() = { runtime.fullAnswerRequireCorrect }
    val quizIdInternal: () -> String get() = { runtime.quizIdInternal }
    val analysisLoaded: () -> Boolean get() = { runtime.artifactFlags.analysisLoaded }
    val sparkAnalysisLoaded: () -> Boolean get() = { runtime.artifactFlags.sparkAnalysisLoaded }
    val baiduAnalysisLoaded: () -> Boolean get() = { runtime.artifactFlags.baiduAnalysisLoaded }
    val notesLoaded: () -> Boolean get() = { runtime.artifactFlags.notesLoaded }
    val markAnalysisLoaded: () -> Unit get() = { runtime.artifactFlags.analysisLoaded = true }
    val markSparkAnalysisLoaded: () -> Unit get() = { runtime.artifactFlags.sparkAnalysisLoaded = true }
    val markBaiduAnalysisLoaded: () -> Unit get() = { runtime.artifactFlags.baiduAnalysisLoaded = true }
    val markNotesLoaded: () -> Unit get() = { runtime.artifactFlags.notesLoaded = true }
    val resetArtifactLoadedFlags: () -> Unit get() = runtime::resetArtifactLoadedFlags
}

internal data class ExamSessionCoordinatorHub(
    val progressCoordinator: ExamSessionProgressCoordinator,
    val reviewCoordinator: ExamReviewSessionCoordinator,
    val navigationCoordinator: ExamNavigationCoordinator,
    val navigationDelegate: ExamSessionNavigationDelegate,
    val answerCoordinator: ExamAnswerCoordinator,
    val memoryModeCoordinator: ExamMemoryModeCoordinator,
    val editCoordinator: ExamQuestionEditCoordinator,
    val statisticsCoordinator: ExamStatisticsCoordinator,
    val gradeCoordinator: ExamGradeCoordinator,
    val artifactCoordinator: ExamArtifactStateCoordinator,
    val progressResetCoordinator: ExamProgressResetCoordinator,
)

internal object ExamSessionCoordinatorAssembly {
    fun assemble(ctx: ExamSessionWireContext): ExamSessionCoordinatorHub {
        lateinit var navigationCoordinator: ExamNavigationCoordinator
        lateinit var artifactCoordinator: ExamArtifactStateCoordinator

        val progressCoordinator =
            ExamSessionProgressCoordinator(
                sessionState = ctx.sessionState,
                scope = ctx.scope,
                sessionEngine = ctx.deps.sessionEngine,
                facade = ctx.deps.facade,
                fillTransform = ctx.deps.fillTransform,
                navHelper = ctx.deps.navHelper,
                persistentQuestionStateMap = ctx.persistentQuestionStateMap,
                progressId = ctx.progressId,
                progressSeedRef = ctx.progressSeed,
                setProgressSeed = ctx.setProgressSeed,
                randomExamEnabled = ctx.randomExamEnabled,
                memoryModeActive = ctx.memoryModeActive,
                allSourceQuestions = ctx.allSourceQuestions,
                reviewModeActive = { ctx.reviewModeActive.value },
                persistenceConfig = { ctx.strategyCoordinator.persistenceConfig() },
                messageResult = ctx.messageResult,
                currentFullAnswerCandidateIndices = { candidates ->
                    navigationCoordinator.currentFullAnswerCandidateIndices(candidates)
                },
                onCalculateCumulativeStats = ctx.calculateCumulativeStats,
                onLoadAnalysis = { artifactCoordinator.loadAnalysisFromRepository() },
                onLoadSparkAnalysis = { artifactCoordinator.loadSparkAnalysisFromRepository() },
                onLoadBaiduAnalysis = { artifactCoordinator.loadBaiduAnalysisFromRepository() },
                onLoadNotes = { artifactCoordinator.loadNotesFromRepository() },
                isAnalysisLoaded = ctx.analysisLoaded,
                isSparkAnalysisLoaded = ctx.sparkAnalysisLoaded,
                isBaiduAnalysisLoaded = ctx.baiduAnalysisLoaded,
                isNotesLoaded = ctx.notesLoaded,
                markAnalysisLoaded = ctx.markAnalysisLoaded,
                markSparkAnalysisLoaded = ctx.markSparkAnalysisLoaded,
                markBaiduAnalysisLoaded = ctx.markBaiduAnalysisLoaded,
                markNotesLoaded = ctx.markNotesLoaded,
            )

        val reviewCoordinator =
            ExamReviewSessionCoordinator(
                sessionState = ctx.sessionState,
                reviewModeActive = ctx.reviewModeActive,
                scope = ctx.scope,
                progressId = ctx.progressId,
                setProgressId = ctx.setProgressId,
                resetArtifactLoadedFlags = ctx.resetArtifactLoadedFlags,
                loadReviewSession = { id, quiz, count, random, wrong, fav ->
                    ctx.deps.loadDelegate.loadReviewSession(id, quiz, count, random, wrong, fav)
                },
                scheduleNavigationSave = ctx.scheduleNavigationSave,
            )

        navigationCoordinator =
            ExamNavigationCoordinator(
                sessionState = ctx.sessionState,
                scope = ctx.scope,
                navHelper = ctx.deps.navHelper,
                answerRules = ctx.deps.answerRules,
                fullAnswerModeActive = ctx.fullAnswerModeActiveNow,
                fullAnswerRequireCorrect = ctx.fullAnswerRequireCorrect,
                randomExamEnabled = ctx.randomExamEnabled,
                memoryModeActive = ctx.memoryModeActive,
                effectiveCurrentMemoryRoundQuestionIds = ctx.effectiveCurrentMemoryRoundQuestionIds,
                buildExamQuestionState = ctx.buildExamQuestionState,
                advanceMemoryRoundIfNeeded = ctx.advanceMemoryRoundIfNeeded,
                reopenQuestionForFullAnswerRetry = ctx.reopenQuestionForFullAnswerRetry,
                scheduleNavigationSave = ctx.scheduleNavigationSave,
                saveProgressInternal = ctx.saveProgressInternal,
            )

        val navigationDelegate =
            ExamSessionNavigationDelegate(
                strategyCoordinator = ctx.strategyCoordinator,
                reviewCoordinator = reviewCoordinator,
                navigationCoordinator = navigationCoordinator,
                isFullAnswerMode = ctx.fullAnswerModeActiveNow,
            )

        val answerCoordinator =
            ExamAnswerCoordinator(
                sessionState = ctx.sessionState,
                scope = ctx.scope,
                memoryModeActive = ctx.memoryModeActive,
                randomExamEnabled = ctx.randomExamEnabled,
                currentFullAnswerCandidateIndices = ctx.currentFullAnswerCandidateIndices,
                refreshMemoryRoundPoolIfNeeded = ctx.refreshMemoryRoundPoolIfNeeded,
                navigateToRandomUnansweredOrAdvanceRound = ctx.navigateToRandomUnansweredOrAdvanceRound,
                calculateCumulativeStats = ctx.calculateCumulativeStats,
                saveProgress = ctx.saveProgress,
                saveProgressInternal = ctx.saveProgressInternal,
            )

        val memoryModeCoordinator =
            ExamMemoryModeCoordinator(
                sessionState = ctx.sessionState,
                facade = ctx.deps.facade,
                memoryModeEngine = ctx.deps.memoryModeEngine,
                answerRules = ctx.deps.answerRules,
                persistentQuestionStateMap = ctx.persistentQuestionStateMap,
                memoryModeActive = ctx.memoryModeActive,
                memoryModeBatchSize = ctx.memoryModeBatchSize,
                memoryWrongMode = ctx.memoryWrongMode,
                memoryPoolMode = ctx.memoryPoolMode,
                randomExamEnabled = ctx.randomExamEnabled,
                allSourceQuestions = ctx.allSourceQuestions,
                currentMemoryRoundQuestionIds = ctx.currentMemoryRoundQuestionIds,
                setCurrentMemoryRoundQuestionIds = ctx.setCurrentMemoryRoundQuestionIds,
                applyConfiguredFillQuestions = ctx.applyConfiguredFillQuestions,
                mergeCurrentStateToPersistentMap = progressCoordinator::mergeCurrentStateToPersistentMap,
                saveProgressInternal = ctx.saveProgressInternal,
            )

        val editCoordinator =
            ExamQuestionEditCoordinator(
                sessionState = ctx.sessionState,
                editableQuestion = ctx.editableQuestion,
                messageResult = ctx.messageResult,
                saveSuccess = ctx.saveSuccess,
                scope = ctx.scope,
                facade = ctx.deps.facade,
                fillTransform = ctx.deps.fillTransform,
                navHelper = ctx.deps.navHelper,
                editedQuestionSnapshotMap = ctx.editedQuestionSnapshotMap,
                allSourceQuestions = ctx.allSourceQuestions,
                setAllSourceQuestions = ctx.setAllSourceQuestions,
                progressSeed = ctx.progressSeed,
                saveProgressInternal = ctx.saveProgressInternal,
            )

        val statisticsCoordinator =
            ExamStatisticsCoordinator(
                sessionState = ctx.sessionState,
                cumulativeCorrect = ctx.cumulativeCorrect,
                cumulativeAnswered = ctx.cumulativeAnswered,
                cumulativeExamCount = ctx.cumulativeExamCount,
                scope = ctx.scope,
                fontSettingsRepository = ctx.deps.fontSettings,
            )

        val gradeCoordinator =
            ExamGradeCoordinator(
                sessionState = ctx.sessionState,
                messageResult = ctx.messageResult,
                facade = ctx.deps.facade,
                progressSeed = ctx.progressSeed,
                quizIdInternal = ctx.quizIdInternal,
                memoryModeActive = ctx.memoryModeActive,
                advanceMemoryRoundIfNeeded = ctx.advanceMemoryRoundIfNeeded,
                incrementExamCount = ctx.incrementExamCount,
                saveProgressInternal = ctx.saveProgressInternal,
            )

        artifactCoordinator =
            ExamArtifactStateCoordinator(
                scope = ctx.scope,
                facade = ctx.deps.facade,
                sessionEngine = ctx.deps.sessionEngine,
                sessionState = ctx.sessionState,
                messageResult = ctx.messageResult,
                saveProgress = ctx.saveProgress,
            )

        val progressResetCoordinator =
            ExamProgressResetCoordinator(
                scope = ctx.scope,
                sessionEngine = ctx.deps.sessionEngine,
                sessionState = ctx.sessionState,
                progressId = ctx.progressId,
                setProgressSeed = ctx.setProgressSeed,
                resetArtifactLoadedFlags = ctx.resetArtifactLoadedFlags,
                loadProgress = ctx.loadProgress,
            )

        return ExamSessionCoordinatorHub(
            progressCoordinator = progressCoordinator,
            reviewCoordinator = reviewCoordinator,
            navigationCoordinator = navigationCoordinator,
            navigationDelegate = navigationDelegate,
            answerCoordinator = answerCoordinator,
            memoryModeCoordinator = memoryModeCoordinator,
            editCoordinator = editCoordinator,
            statisticsCoordinator = statisticsCoordinator,
            gradeCoordinator = gradeCoordinator,
            artifactCoordinator = artifactCoordinator,
            progressResetCoordinator = progressResetCoordinator,
        )
    }
}
