package com.example.testapp.core.session.registry

import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCapabilities
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.SessionUiContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class StubQuestionSession(
    override val kind: QuestionSessionKind,
    override val capabilities: SessionCapabilities = SessionCapabilitiesPresets.forKind(kind),
    scope: CoroutineScope,
) : QuestionSession {
    override val uiContract: SessionUiContract = UiPolicyFactory.from(capabilities)

    private val _snapshot = MutableStateFlow(SessionSnapshot())
    override val snapshot: StateFlow<SessionSnapshot> = _snapshot.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    override suspend fun start() {
        _events.emit(SessionEvent.SessionStarted(kind))
    }

    override suspend fun destroy() {
        _events.emit(SessionEvent.SessionDestroyed)
    }

    override fun handle(command: SessionCommand) = Unit
}
