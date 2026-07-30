package com.example.testapp.presentation.screen.magnetic

import kotlin.test.Test
import kotlin.test.assertEquals

class MagneticDragInsertionTest {
    @Test
    fun insertsBeforeTokenWhoseCenterIsAfterPointer() {
        val result =
            calculateMagneticInsertionIndex(
                orderedTokenIds = listOf(1, 2, 3),
                draggedTokenId = 1,
                boundsById =
                    mapOf(
                        2 to MagneticDragBounds(100f, 100f, 200f, 150f),
                        3 to MagneticDragBounds(210f, 100f, 310f, 150f),
                    ),
                pointerX = 205f,
                pointerY = 125f,
            )

        assertEquals(1, result)
    }

    @Test
    fun supportsInsertionAtBeginningOfLaterFlowRow() {
        val result =
            calculateMagneticInsertionIndex(
                orderedTokenIds = listOf(1, 2, 3, 4),
                draggedTokenId = 1,
                boundsById =
                    mapOf(
                        2 to MagneticDragBounds(20f, 100f, 150f, 150f),
                        3 to MagneticDragBounds(160f, 100f, 300f, 150f),
                        4 to MagneticDragBounds(20f, 170f, 180f, 220f),
                    ),
                pointerX = 10f,
                pointerY = 190f,
            )

        assertEquals(2, result)
    }

    @Test
    fun supportsInsertionAfterLastToken() {
        val result =
            calculateMagneticInsertionIndex(
                orderedTokenIds = listOf(1, 2, 3),
                draggedTokenId = 2,
                boundsById =
                    mapOf(
                        1 to MagneticDragBounds(20f, 100f, 120f, 150f),
                        3 to MagneticDragBounds(20f, 170f, 160f, 220f),
                    ),
                pointerX = 300f,
                pointerY = 240f,
            )

        assertEquals(2, result)
    }
}
