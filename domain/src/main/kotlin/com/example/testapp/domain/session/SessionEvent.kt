package com.example.testapp.domain.session

/** Session → Extension / 内部（ADR Event 侧） */
sealed interface SessionEvent {
    data class SessionStarted(val kind: QuestionSessionKind) : SessionEvent
    data class QuestionChanged(val index: Int, val questionId: Int) : SessionEvent
    data class AnswerSubmitted(val index: Int, val questionId: Int) : SessionEvent
    data object SessionDestroyed : SessionEvent
}
