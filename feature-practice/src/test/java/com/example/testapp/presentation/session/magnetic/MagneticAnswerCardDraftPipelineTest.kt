package com.example.testapp.presentation.session.magnetic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MagneticAnswerCardDraftPipelineTest {
    @Test
    fun switchingQuestionKeepsPartialBoardForLaterReturn() {
        val clauses = listOf(clause(1), clause(2), clause(3))
        val state =
            MagneticRebuildUiState(
                isLoading = false,
                clauses = clauses,
                currentClauseIndex = 0,
                candidates = clauses[0].tokens.drop(1),
                placed = clauses[0].tokens.take(1),
                moveCount = 1,
            )

        val stored = MagneticRebuildDraftPipeline.storeCurrent(state)
        val openedSecond =
            MagneticRebuildDraftPipeline.openClause(
                state = stored,
                index = 1,
                freshCandidates = clauses[1].tokens.reversed(),
            )
        val reopenedFirst =
            MagneticRebuildDraftPipeline.openClause(
                state = openedSecond,
                index = 0,
                freshCandidates = emptyList(),
            )

        assertEquals(listOf(clauses[0].tokens.first().id), reopenedFirst.placed.map(MagneticToken::id))
        assertEquals(1, reopenedFirst.moveCount)
        assertTrue(clauses[0].sourceQuestionId in reopenedFirst.startedQuestionIds)
    }

    @Test
    fun nextIncompleteQuestionWrapsAcrossSkippedItems() {
        val clauses = listOf(clause(1), clause(2), clause(3), clause(4))
        val state =
            MagneticRebuildUiState(
                isLoading = false,
                clauses = clauses,
                currentClauseIndex = 3,
                completedQuestionIds = setOf(clauses[0].sourceQuestionId, clauses[3].sourceQuestionId),
            )

        assertEquals(1, MagneticRebuildDraftPipeline.nextIncompleteIndex(state))
    }

    @Test
    fun untouchedSkippedQuestionDoesNotBecomeStarted() {
        val clauses = listOf(clause(1), clause(2))
        val state =
            MagneticRebuildUiState(
                isLoading = false,
                clauses = clauses,
                currentClauseIndex = 0,
                candidates = clauses[0].tokens.reversed(),
            )

        val stored = MagneticRebuildDraftPipeline.storeCurrent(state)

        assertFalse(clauses[0].sourceQuestionId in stored.startedQuestionIds)
    }

    private fun clause(questionId: Int): MagneticClause =
        MagneticClause(
            sourceQuestionId = questionId,
            originalText = "第${questionId}条测试条文。",
            tokens =
                listOf("第${questionId}条", "测试", "条文。")
                    .mapIndexed { index, text ->
                        MagneticToken(
                            id = questionId * 100 + index,
                            text = text,
                            order = index,
                            role = MagneticSemanticRole.OTHER,
                        )
                    },
        )
}
