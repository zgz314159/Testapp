package com.example.testapp.core.session.policy.reveal

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.reveal.SessionRevealPolicy

object RevealPolicyFactory {
    val browse: SessionRevealPolicy = BrowseRevealPolicy
    val practice: SessionRevealPolicy = PracticeRevealPolicy
    val review: SessionRevealPolicy = ReviewRevealPolicy
    val exam: SessionRevealPolicy = ExamRevealPolicy

    fun forKind(kind: QuestionSessionKind): SessionRevealPolicy =
        when (kind) {
            is QuestionSessionKind.Browse,
            is QuestionSessionKind.QuestionEdit,
            -> browse
            is QuestionSessionKind.Review -> review
            is QuestionSessionKind.AdaptiveFading -> practice
            is QuestionSessionKind.Practice -> practice
            is QuestionSessionKind.Exam ->
                if (kind.reviewProgressId != null) review else exam
        }
}
