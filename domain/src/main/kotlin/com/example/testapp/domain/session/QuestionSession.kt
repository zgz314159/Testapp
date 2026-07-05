package com.example.testapp.domain.session

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface QuestionSession {
    val kind: QuestionSessionKind
    val capabilities: SessionCapabilities
    val uiContract: SessionUiContract
    val snapshot: StateFlow<SessionSnapshot>
    val events: SharedFlow<SessionEvent>

    suspend fun start()
    suspend fun destroy()
    fun handle(command: SessionCommand)
}
