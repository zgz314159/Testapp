package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.util.FullAnswerMultiRoundSessionPipeline
import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionIconTapNavigationPipelineTest {
    private val practiceOrch =
        SessionNavigationOrchestrationResolver.from(
            com.example.testapp.domain.session.navigation.SessionNavigationConfig(
                mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
                swipeAnsweredHistory = true,
            ),
        )

    private val reviewOrch =
        SessionNavigationOrchestrationResolver.from(
            com.example.testapp.domain.session.navigation.SessionNavigationConfig(
                mode = SessionNavigationMode.REVIEW_HISTORY,
                swipeAnsweredHistory = true,
            ),
        )

    @Test
    fun singleTap_multiRoundFullAnswer_usesRoundPool() {
        val questions =
            listOf(
                Question(
                    id = buildDerivedFillQuestionId(1, 1),
                    content = "a",
                    type = "fill",
                    options = emptyList(),
                    answer = "b",
                    explanation = "",
                ),
            )
        assertTrue(FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(questions))
        assertEquals(
            SessionIconTapPath.ROUND_POOL,
            SessionIconTapNavigationPipeline.resolveSingleTapPath(
                orchestration = practiceOrch,
                fullAnswerModeActive = true,
                multiRoundSession = true,
            ),
        )
    }

    @Test
    fun singleTap_reviewMode_disabled() {
        assertEquals(
            SessionIconTapPath.DISABLED,
            SessionIconTapNavigationPipeline.resolveSingleTapPath(
                orchestration = reviewOrch,
                fullAnswerModeActive = false,
                multiRoundSession = false,
            ),
        )
    }

    @Test
    fun doubleTap_requiresFullAnswerMode_whenConfigured() {
        assertEquals(
            SessionIconDoubleTapAction.DISABLED,
            SessionIconTapNavigationPipeline.resolveDoubleTapAction(
                orchestration = practiceOrch,
                fullAnswerModeActive = false,
                multiRoundSession = false,
            ),
        )
        assertEquals(
            SessionIconDoubleTapAction.CROSS_SOURCE,
            SessionIconTapNavigationPipeline.resolveDoubleTapAction(
                orchestration = practiceOrch,
                fullAnswerModeActive = true,
                multiRoundSession = true,
            ),
        )
    }
}
