package com.example.testapp.domain.session

/** 会话行为唯一开关源（ADR-002）；Screen / Strategy 只读此结构 */
data class SessionCapabilities(
    val canSubmit: Boolean = true,
    val canPersistProgress: Boolean = true,
    val canRestoreProgress: Boolean = true,
    val canSwipeAnsweredHistory: Boolean = true,
    val canRevealOnSubmit: Boolean = true,
    val canUseAiAsk: Boolean = true,
    val canEditQuestion: Boolean = true,
    val canShowAnswerCard: Boolean = true,
    val fullAnswerModeActive: Boolean = false,
    val fullAnswerRequireCorrect: Boolean = false
)
