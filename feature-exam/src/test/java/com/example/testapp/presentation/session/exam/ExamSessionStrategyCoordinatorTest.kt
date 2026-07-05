package com.example.testapp.presentation.session.exam

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamSessionStrategyCoordinatorTest {
    @Test
    fun bindStrategy_appliesContextToListener() {
        var applied = 0
        val coordinator =
            ExamSessionStrategyCoordinator(
                progressId = { "exam_quiz" },
                onStrategyApplied = { applied++ },
            )

        coordinator.bindStrategy(QuestionSessionKind.Exam("quiz.json"))

        assertEquals(1, applied)
        assertEquals(SessionExitMode.EXAM, coordinator.exitConfig().mode)
    }

    @Test
    fun reviewSnapshot_restoreRoundTrip() {
        val coordinator =
            ExamSessionStrategyCoordinator(
                progressId = { "exam_quiz" },
                onStrategyApplied = {},
            )
        coordinator.bindStrategy(QuestionSessionKind.Exam("quiz.json"))
        coordinator.capturePreReviewExamKind("quiz.json", wrongBook = false, favorite = false)
        coordinator.bindReviewStrategy(
            targetProgressId = "exam_quiz",
            quizFile = "quiz.json",
            wrongBook = false,
            favorite = false,
        )

        assertEquals(SessionExitMode.REVIEW, coordinator.exitConfig().mode)

        val restore = coordinator.restorePreReviewExamKindOrNull()
        assertTrue(restore != null)
        coordinator.bindStrategy(restore!!)

        assertEquals(SessionExitMode.EXAM, coordinator.exitConfig().mode)
        assertEquals(SessionNavigationMode.EXAM_LINEAR, coordinator.navigationConfig().mode)
        assertNull(coordinator.restorePreReviewExamKindOrNull())
    }
}
