package com.example.testapp.core.session.policy.navigation

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.navigation.SessionNavigationPolicy

object NavigationPolicyFactory {
    val browse: SessionNavigationPolicy = BrowseNavigationPolicy
    val practice: SessionNavigationPolicy = PracticeNavigationPolicy
    val review: SessionNavigationPolicy = ReviewNavigationPolicy
    val exam: SessionNavigationPolicy = ExamNavigationPolicy

    fun forKind(kind: QuestionSessionKind): SessionNavigationPolicy =
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
