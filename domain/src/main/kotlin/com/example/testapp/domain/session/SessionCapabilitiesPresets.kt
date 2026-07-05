package com.example.testapp.domain.session

object SessionCapabilitiesPresets {

    val practice: SessionCapabilities = SessionCapabilities()

    val browse: SessionCapabilities = SessionCapabilities(
        canSubmit = false,
        canPersistProgress = false,
        canRestoreProgress = false,
        canRevealOnSubmit = false,
        canUseAiAsk = false,
        canEditQuestion = false,
        canShowAnswerCard = false
    )

    val review: SessionCapabilities = SessionCapabilities(
        canSubmit = false,
        canPersistProgress = false,
        canRestoreProgress = true,
        canRevealOnSubmit = false
    )

    val exam: SessionCapabilities = SessionCapabilities(
        canSwipeAnsweredHistory = false,
        canEditQuestion = false
    )

    val questionEdit: SessionCapabilities = SessionCapabilities(
        canSubmit = false,
        canPersistProgress = false,
        canRestoreProgress = false,
        canRevealOnSubmit = false,
        canUseAiAsk = false,
        canEditQuestion = true,
        canShowAnswerCard = false
    )

    fun forKind(kind: QuestionSessionKind): SessionCapabilities = when (kind) {
        is QuestionSessionKind.Practice -> practice
        is QuestionSessionKind.Browse -> browse
        is QuestionSessionKind.Review -> review
        is QuestionSessionKind.Exam -> if (kind.reviewProgressId != null) review else exam
        is QuestionSessionKind.QuestionEdit -> questionEdit
    }
}
