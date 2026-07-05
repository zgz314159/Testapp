package com.example.testapp.presentation.session.browse

import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.browse.SessionBrowseStrategyGate
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.LifecycleExtension
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionContext
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.SessionUiContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowseSession(
    override val kind: QuestionSessionKind.Browse,
    private val deps: BrowseSessionDeps,
    private val scope: CoroutineScope,
    extensions: List<SessionExtension>,
) : QuestionSession {
    override val capabilities = SessionCapabilitiesPresets.browse
    override val uiContract: SessionUiContract = UiPolicyFactory.from(capabilities)

    private val _practiceState = MutableStateFlow(PracticeSessionState())
    val practiceState: StateFlow<PracticeSessionState> = _practiceState.asStateFlow()

    private val _snapshot = MutableStateFlow(SessionSnapshot())
    override val snapshot: StateFlow<SessionSnapshot> = _snapshot.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    private val lifecycleExtensions = extensions.filterIsInstance<LifecycleExtension>()
    private val featureExtensions = extensions.filterIsInstance<FeatureExtension>()

    private lateinit var strategyContext: SessionStrategyContext

    override suspend fun start() {
        strategyContext = BrowseSessionStrategyBootstrap.bind(kind)
        require(SessionBrowseStrategyGate.isLinearBrowseNavigation(strategyContext)) {
            "BrowseSession requires BROWSE_LINEAR navigation"
        }
        val sessionStartTime = System.currentTimeMillis()
        val settings = deps.fontSettings.readSettingsSnapshot()
        val catalog = deps.questionFlowCache.preload(kind.quizId)
        val loaded =
            BrowseSessionLoadPipeline.load(
                catalog = catalog,
                targetQuestionId = kind.targetQuestionId,
                questionCount = settings.practiceQuestionCount,
                random = settings.randomPractice,
                sessionStartTime = sessionStartTime,
            )
        _practiceState.value =
            PracticeSessionState(
                sessionId = "browse_${kind.quizId}_${kind.targetQuestionId}",
                questionsWithState = loaded.questionsWithState,
                currentIndex = loaded.startIndex,
                sessionStartTime = sessionStartTime,
                progressId = "browse_${kind.quizId}",
                questionsSource = kind.quizId,
                isRandomMode = settings.randomPractice,
                questionCount = settings.practiceQuestionCount,
                progressLoaded = true,
            )
        publishSnapshot()
        _events.emit(SessionEvent.SessionStarted(kind))
        val context = SessionContext(kind, capabilities, events)
        lifecycleExtensions.filter { it.supports(kind) }.forEach { it.onStart(context) }
        currentQuestionId()?.let { id ->
            _events.emit(SessionEvent.QuestionChanged(_practiceState.value.currentIndex, id))
        }
    }

    override suspend fun destroy() {
        lifecycleExtensions.filter { it.supports(kind) }.forEach { it.onDestroy() }
        _events.emit(SessionEvent.SessionDestroyed)
    }

    override fun handle(command: SessionCommand) {
        when (command) {
            SessionCommand.Back -> Unit
            SessionCommand.NextQuestion -> stepIndex(1)
            SessionCommand.PrevQuestion -> stepIndex(-1)
            is SessionCommand.GoToQuestion -> goTo(command.index)
            else -> Unit
        }
    }

    fun canStepBack(): Boolean = _practiceState.value.currentIndex > 0

    fun canStepForward(): Boolean {
        val state = _practiceState.value
        return state.questionsWithState.isNotEmpty() &&
            state.currentIndex < state.questionsWithState.lastIndex
    }

    private fun stepIndex(delta: Int) {
        goTo(_practiceState.value.currentIndex + delta)
    }

    private fun goTo(index: Int) {
        val state = _practiceState.value
        if (state.questionsWithState.isEmpty()) return
        val clamped = index.coerceIn(0, state.questionsWithState.lastIndex)
        if (clamped == state.currentIndex) return
        _practiceState.update { it.copy(currentIndex = clamped) }
        publishSnapshot()
        val questionId = state.questionsWithState[clamped].question.id
        scope.launch {
            val event = SessionEvent.QuestionChanged(clamped, questionId)
            _events.emit(event)
            featureExtensions.filter { it.supports(kind) }.forEach { ext ->
                ext.onEvent(event, _snapshot.value) { handle(it) }
            }
        }
    }

    private fun publishSnapshot() {
        val snap = BrowseSessionSnapshotMapper.toSnapshot(_practiceState.value)
        _snapshot.value = snap
    }

    private fun currentQuestionId(): Int? = _practiceState.value.currentQuestion?.question?.id
}
