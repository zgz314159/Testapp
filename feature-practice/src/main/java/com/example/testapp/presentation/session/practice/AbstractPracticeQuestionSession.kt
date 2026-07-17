package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.SessionExtensionEventWiring
import com.example.testapp.core.session.SessionExtensionNotifier
import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCapabilities
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.SessionUiContract
import com.example.testapp.presentation.session.browse.BrowseSessionSnapshotMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class AbstractPracticeQuestionSession(
    sessionKind: QuestionSessionKind,
    protected val deps: PracticeSessionDeps,
    private val scope: CoroutineScope,
    extensions: List<SessionExtension>,
) : QuestionSession {
    final override val kind: QuestionSessionKind = sessionKind

    protected val engine = PracticeSessionEngine(scope, deps)
    val bindings: PracticeScreenBindings get() = engine

    override val capabilities: SessionCapabilities = SessionCapabilitiesPresets.forKind(sessionKind)
    override val uiContract: SessionUiContract = UiPolicyFactory.from(capabilities)

    private val _snapshot = MutableStateFlow(SessionSnapshot())
    override val snapshot: StateFlow<SessionSnapshot> = _snapshot.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    private val featureExtensions = extensions.filterIsInstance<FeatureExtension>()

    init {
        engine.bindStrategy(sessionKind)
        scope.launch {
            engine.sessionState.collect { publishSnapshot(it) }
        }
        SessionExtensionEventWiring.launchQuestionChangedEvents(scope, engine.sessionState) { index, questionId ->
            publishQuestionChanged(index, questionId)
        }
        SessionExtensionEventWiring.launchAnswerSubmittedEvents(scope, engine.sessionState) { index, questionId ->
            publishAnswerSubmitted(index, questionId)
        }
    }

    protected suspend fun emitStarted() {
        _events.emit(SessionEvent.SessionStarted(kind))
    }

    protected fun publishSnapshot(state: PracticeSessionState = engine.sessionState.value) {
        _snapshot.value = BrowseSessionSnapshotMapper.toSnapshot(state).copy(kind = kind)
    }

    protected fun emitQuestionChanged(
        index: Int,
        questionId: Int,
    ) {
        publishQuestionChanged(index, questionId)
    }

    private fun publishQuestionChanged(
        index: Int,
        questionId: Int,
    ) {
        scope.launch {
            val event = SessionEvent.QuestionChanged(index, questionId)
            _events.emit(event)
            notifyFeatureExtensions(event)
        }
    }

    private fun publishAnswerSubmitted(
        index: Int,
        questionId: Int,
    ) {
        publishSnapshot()
        scope.launch {
            val event = SessionEvent.AnswerSubmitted(index, questionId)
            _events.emit(event)
            notifyFeatureExtensions(event)
        }
    }

    private suspend fun notifyFeatureExtensions(event: SessionEvent) {
        SessionExtensionNotifier.notify(
            event = event,
            snapshot = _snapshot.value,
            kind = kind,
            extensions = featureExtensions,
        ) { handle(it) }
    }

    override fun handle(command: SessionCommand) {
        PracticeSessionCommandHandler.dispatch(engine, command)
    }
}
