package com.example.testapp.presentation.session.magnetic

import kotlin.test.Test
import kotlin.test.assertEquals

class MagneticIndependentAdjacencyTest {
    @Test
    fun wrongEarlierPairDoesNotBreakCorrectLaterPair() {
        val first = token(id = 10, order = 0)
        val misplaced = token(id = 20, order = 5)
        val laterLeft = token(id = 30, order = 2)
        val laterRight = token(id = 40, order = 3)

        val result = evaluateMagneticAdjacency(listOf(first, misplaced, laterLeft, laterRight))

        assertEquals(1, result.correctPairCount)
        assertEquals(setOf(laterLeft.id, laterRight.id), result.connectedTokenIds)
    }

    @Test
    fun disconnectedMiddleTokenKeepsBothIndependentCorrectSegments() {
        val first = token(id = 10, order = 0)
        val second = token(id = 20, order = 1)
        val misplaced = token(id = 30, order = 8)
        val laterLeft = token(id = 40, order = 3)
        val laterRight = token(id = 50, order = 4)

        val result = evaluateMagneticAdjacency(listOf(first, second, misplaced, laterLeft, laterRight))

        assertEquals(2, result.correctPairCount)
        assertEquals(setOf(first.id, second.id, laterLeft.id, laterRight.id), result.connectedTokenIds)
    }

    private fun token(
        id: Int,
        order: Int,
    ): MagneticToken =
        MagneticToken(
            id = id,
            text = "词块$id",
            order = order,
            role = MagneticSemanticRole.OTHER,
        )
}
