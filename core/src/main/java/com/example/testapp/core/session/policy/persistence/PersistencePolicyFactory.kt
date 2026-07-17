package com.example.testapp.core.session.policy.persistence

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.persistence.SessionPersistencePolicy

object PersistencePolicyFactory {
    val browse: SessionPersistencePolicy = BrowsePersistencePolicy
    val practice: SessionPersistencePolicy = PracticePersistencePolicy
    val review: SessionPersistencePolicy = ReviewPersistencePolicy
    val exam: SessionPersistencePolicy = ExamPersistencePolicy

    fun forKind(kind: QuestionSessionKind): SessionPersistencePolicy =
        when (kind) {
            is QuestionSessionKind.Browse,
            is QuestionSessionKind.QuestionEdit,
            -> browse
            is QuestionSessionKind.Review -> review
            is QuestionSessionKind.AdaptiveFading -> browse
            is QuestionSessionKind.Practice -> practice
            is QuestionSessionKind.Exam ->
                if (kind.reviewProgressId != null) review else exam
        }
}
