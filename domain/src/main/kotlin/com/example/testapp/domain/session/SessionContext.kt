package com.example.testapp.domain.session

import kotlinx.coroutines.flow.SharedFlow

/** Extension 只读上下文；禁止持有整个 QuestionSession */
data class SessionContext(
    val kind: QuestionSessionKind,
    val capabilities: SessionCapabilities,
    val events: SharedFlow<SessionEvent>
)
