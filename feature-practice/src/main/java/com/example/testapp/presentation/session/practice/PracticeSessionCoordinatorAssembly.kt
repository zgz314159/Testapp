package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

internal data class PracticeSessionWireContext(
    val scope: CoroutineScope,
    val deps: PracticeSessionDeps,
    val sessionState: MutableStateFlow<PracticeSessionState>,
    val runtime: PracticeSessionRuntimeState,
    val reviewModeActive: MutableStateFlow<Boolean>,
    val reviewReady: MutableStateFlow<Boolean>,
    val stateUpdater: PracticeStateUpdater,
    val repositoryContentLoader: PracticeRepositoryContentLoader,
    val answerHandler: PracticeAnswerHandler,
    val saveProgress: () -> Unit,
    val scheduleNavigationSave: () -> Unit,
    val rememberAnsweredHistorySnapshot: (Int) -> Unit,
    val setRandomPracticeOnNav: (Boolean) -> Unit,
) {
    val progressId: () -> String get() = { runtime.progressId }
    val setProgressId: (String) -> Unit get() = { runtime.progressId = it }
    val questionSourceId: () -> String get() = { runtime.questionSourceId }
    val setQuestionSourceId: (String) -> Unit get() = { runtime.questionSourceId = it }
    val randomPracticeEnabled: () -> Boolean get() = { runtime.randomPracticeEnabled }
    val setRandomPracticeEnabled: (Boolean) -> Unit get() = { value ->
        runtime.randomPracticeEnabled = value
        setRandomPracticeOnNav(value)
    }
    val onFillConfigApplied: (PracticeFillConfig) -> Unit get() = { runtime.activeFillConfig = it }
    val activeFillConfig: () -> PracticeFillConfig get() = { runtime.activeFillConfig }
}

internal data class PracticeSessionCoordinatorHub(
    val strategyCoordinator: PracticeSessionStrategyCoordinator,
    val progressLifecycle: PracticeProgressLifecycleCoordinator,
    val reviewCoordinator: PracticeReviewSessionCoordinator,
    val navigationCoordinator: PracticeNavigationCoordinator,
    val navigationDelegate: PracticeSessionNavigationDelegate,
    val gradeDelegate: PracticeSessionGradeDelegate,
    val questionContentDelegate: PracticeSessionQuestionContentDelegate,
    val specialQuestionLoader: PracticeSpecialQuestionLoader,
    val questionEditCoordinator: PracticeQuestionEditCoordinator,
    val noteCoordinator: PracticeNoteCoordinator,
)

internal object PracticeSessionCoordinatorAssembly {
    fun assemble(ctx: PracticeSessionWireContext): PracticeSessionCoordinatorHub {
        val navigationCoordinator = PracticeNavigationCoordinator()
        lateinit var progressLifecycle: PracticeProgressLifecycleCoordinator

        val strategyCoordinator =
            PracticeSessionStrategyCoordinator(
                progressId = ctx.progressId,
                onStrategyApplied = { context ->
                    progressLifecycle.applyPersistenceConfig(context.persistence)
                },
            )

        navigationCoordinator.bindNavigationOrchestration {
            strategyCoordinator.navigationOrchestration()
        }

        progressLifecycle =
            PracticeProgressLifecycleCoordinator(
                scope = ctx.scope,
                sessionEngine = ctx.deps.sessionEngine,
                facade = ctx.deps.facade,
                questionFlowCache = ctx.deps.questionFlowCache,
                fontSettings = ctx.deps.fontSettings,
                sessionState = ctx.sessionState,
                repositoryContentLoader = ctx.repositoryContentLoader,
                progressId = ctx.progressId,
                setProgressIdValue = ctx.setProgressId,
                questionSourceId = ctx.questionSourceId,
                setQuestionSourceId = ctx.setQuestionSourceId,
                randomPracticeEnabled = ctx.randomPracticeEnabled,
                setRandomPracticeEnabled = ctx.setRandomPracticeEnabled,
                onFillConfigApplied = ctx.onFillConfigApplied,
                onProgressRestored = { questionsWithState, currentIndex ->
                    navigationCoordinator.seedAnsweredHistoryFromRestoredProgress(
                        questionsWithState,
                        ctx.answerHandler::isQuestionAnswered,
                    )
                    navigationCoordinator.seedRandomNavigationHistory(
                        questionsWithState,
                        currentIndex,
                        ctx.answerHandler::isQuestionAnswered,
                    )
                },
            )

        val reviewCoordinator =
            PracticeReviewSessionCoordinator(
                sessionState = ctx.sessionState,
                reviewModeActive = ctx.reviewModeActive,
                reviewReady = ctx.reviewReady,
                scope = ctx.scope,
                progressId = ctx.progressId,
                loadReviewSession = progressLifecycle::loadReviewSession,
                scheduleNavigationSave = ctx.scheduleNavigationSave,
            )

        val navigationDelegate =
            PracticeSessionNavigationDelegate(
                strategyCoordinator = strategyCoordinator,
                reviewCoordinator = reviewCoordinator,
                navigationCoordinator = navigationCoordinator,
                sessionState = ctx.sessionState,
                activeFillConfig = ctx.activeFillConfig,
            )

        val gradeDelegate =
            PracticeSessionGradeDelegate(
                stateUpdater = ctx.stateUpdater,
                sessionState = ctx.sessionState,
                scope = ctx.scope,
                onAnsweredSnapshot = ctx.rememberAnsweredHistorySnapshot,
                saveProgress = ctx.saveProgress,
            )

        val questionContentDelegate =
            PracticeSessionQuestionContentDelegate(
                facade = ctx.deps.facade,
                sessionState = ctx.sessionState,
                scope = ctx.scope,
                saveProgress = ctx.saveProgress,
            )

        val specialQuestionLoader =
            PracticeSpecialQuestionLoader(
                facade = ctx.deps.facade,
                fontSettings = ctx.deps.fontSettings,
                sessionState = ctx.sessionState,
                progressId = ctx.progressId,
                randomPracticeEnabled = ctx.randomPracticeEnabled,
                onFillConfigApplied = ctx.onFillConfigApplied,
                loadProgress = progressLifecycle::loadProgress,
            )

        val questionEditCoordinator = PracticeQuestionEditCoordinator(ctx.deps.facade, ctx.sessionState)
        val noteCoordinator = PracticeNoteCoordinator(ctx.deps.facade, ctx.sessionState)

        return PracticeSessionCoordinatorHub(
            strategyCoordinator = strategyCoordinator,
            progressLifecycle = progressLifecycle,
            reviewCoordinator = reviewCoordinator,
            navigationCoordinator = navigationCoordinator,
            navigationDelegate = navigationDelegate,
            gradeDelegate = gradeDelegate,
            questionContentDelegate = questionContentDelegate,
            specialQuestionLoader = specialQuestionLoader,
            questionEditCoordinator = questionEditCoordinator,
            noteCoordinator = noteCoordinator,
        )
    }
}
