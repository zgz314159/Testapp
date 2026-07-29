package com.example.testapp.presentation.session.magnetic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MagneticDuplicateTokenEquivalenceTest {
    @Test
    fun duplicateTextTokensAreReboundToEitherEquivalentPosition() {
        val clause = sampleClause()
        val swapped = clause.tokens.toMutableList().apply {
            val first = this[7]
            this[7] = this[11]
            this[11] = first
        }

        assertNotEquals(clause.tokens.map(MagneticToken::id), swapped.map(MagneticToken::id))

        val canonical =
            clause.canonicalizeEquivalentTokens(
                MagneticBoardSnapshot(candidates = emptyList(), placed = swapped),
            )

        assertEquals(clause.tokens.map(MagneticToken::id), canonical.placed.map(MagneticToken::id))
        assertEquals(swapped.map(MagneticToken::text), canonical.placed.map(MagneticToken::text))
    }

    @Test
    fun candidateDuplicateCanFillEitherExpectedOccurrence() {
        val clause = sampleClause()
        val placed = clause.tokens.take(7) + clause.tokens[11]
        val candidates = clause.tokens.drop(7).filterNot { it.id == clause.tokens[11].id }

        val canonical =
            clause.canonicalizeEquivalentTokens(
                MagneticBoardSnapshot(candidates = candidates, placed = placed),
            )

        assertEquals(clause.tokens[7].id, canonical.placed.last().id)
        assertEquals(clause.tokens[11].id, canonical.candidates.first { it.text == "电压等级。" }.id)
    }

    @Test
    fun differentTextTokenKeepsItsOwnIdentityAtWrongPosition() {
        val clause = sampleClause()
        val swapped = clause.tokens.toMutableList().apply {
            val first = this[4]
            this[4] = this[8]
            this[8] = first
        }

        val canonical =
            clause.canonicalizeEquivalentTokens(
                MagneticBoardSnapshot(candidates = emptyList(), placed = swapped),
            )

        assertEquals(swapped.map(MagneticToken::id), canonical.placed.map(MagneticToken::id))
    }

    private fun sampleClause(): MagneticClause {
        val texts =
            listOf(
                "第7条",
                "铁路交流电力系统",
                "分为高压和",
                "低压两种：",
                "高压：",
                "1000V以上",
                "的",
                "电压等级。",
                "低压：",
                "1000V及其以下",
                "的",
                "电压等级。",
            )
        return MagneticClause(
            sourceQuestionId = 7,
            originalText = texts.joinToString(separator = ""),
            tokens =
                texts.mapIndexed { index, text ->
                    MagneticToken(
                        id = 700 + index,
                        text = text,
                        order = index,
                        role = MagneticSemanticRole.OTHER,
                    )
                },
        )
    }
}
