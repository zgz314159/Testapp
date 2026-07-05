package com.example.testapp.core.session.policy

import com.example.testapp.core.session.policy.exit.BrowseExitPolicy
import com.example.testapp.core.session.policy.exit.ExamExitPolicy
import com.example.testapp.core.session.policy.exit.PracticeExitPolicy
import com.example.testapp.core.session.policy.exit.ReviewExitPolicy
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitContext
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.exit.SessionExitPolicy

/** Capabilities / Kind → ExitPolicy（ADR-002 策略收编） */
object ExitPolicyFactory {
    val browse: SessionExitPolicy = BrowseExitPolicy
    val review: SessionExitPolicy = ReviewExitPolicy
    val practice: SessionExitPolicy = PracticeExitPolicy
    val exam: SessionExitPolicy = ExamExitPolicy

    fun policyForConfig(config: SessionExitConfig): SessionExitPolicy =
        when (config.mode) {
            SessionExitMode.BROWSE -> browse
            SessionExitMode.REVIEW -> review
            SessionExitMode.PRACTICE -> practice
            SessionExitMode.EXAM -> exam
        }

    fun configForKind(
        kind: QuestionSessionKind,
        context: SessionExitContext = SessionExitContext(),
    ): SessionExitConfig =
        when (kind) {
            is QuestionSessionKind.Browse,
            is QuestionSessionKind.QuestionEdit,
            -> SessionExitConfig(SessionExitMode.BROWSE)
            is QuestionSessionKind.Review -> SessionExitConfig(SessionExitMode.REVIEW)
            is QuestionSessionKind.Practice -> SessionExitConfig(SessionExitMode.PRACTICE)
            is QuestionSessionKind.Exam ->
                if (context.isReviewMode || kind.reviewProgressId != null) {
                    SessionExitConfig(SessionExitMode.REVIEW)
                } else {
                    SessionExitConfig(SessionExitMode.EXAM)
                }
        }

    fun forKind(kind: QuestionSessionKind): SessionExitPolicy = policyForConfig(configForKind(kind))
}
