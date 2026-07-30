package com.example.testapp.presentation.screen.magnetic

internal data class MagneticDragBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val centerX: Float get() = (left + right) / 2f
    val height: Float get() = bottom - top
}

internal fun calculateMagneticInsertionIndex(
    orderedTokenIds: List<Int>,
    draggedTokenId: Int,
    boundsById: Map<Int, MagneticDragBounds>,
    pointerX: Float,
    pointerY: Float,
): Int {
    val originalIndex = orderedTokenIds.indexOf(draggedTokenId).coerceAtLeast(0)
    val remainingIds = orderedTokenIds.filterNot { it == draggedTokenId }
    val positioned =
        remainingIds.mapIndexedNotNull { insertionIndex, tokenId ->
            boundsById[tokenId]?.let { bounds ->
                PositionedToken(insertionIndex = insertionIndex, bounds = bounds)
            }
        }
    if (positioned.isEmpty()) return originalIndex

    val rows = buildRows(positioned)
    if (pointerY < rows.first().top) return 0
    if (pointerY > rows.last().bottom) return remainingIds.size

    val row =
        rows.minBy { candidate ->
            when {
                pointerY < candidate.top -> candidate.top - pointerY
                pointerY > candidate.bottom -> pointerY - candidate.bottom
                else -> 0f
            }
        }
    val tokenToInsertBefore = row.tokens.firstOrNull { pointerX < it.bounds.centerX }
    return tokenToInsertBefore?.insertionIndex ?: (row.tokens.last().insertionIndex + 1)
}

private fun buildRows(tokens: List<PositionedToken>): List<TokenRow> {
    val rows = mutableListOf<TokenRow>()
    tokens.forEach { token ->
        val current = rows.lastOrNull()
        if (current == null || !current.overlapsVertically(token.bounds)) {
            rows += TokenRow(tokens = mutableListOf(token), top = token.bounds.top, bottom = token.bounds.bottom)
        } else {
            current.tokens += token
            current.top = minOf(current.top, token.bounds.top)
            current.bottom = maxOf(current.bottom, token.bounds.bottom)
        }
    }
    return rows
}

private data class PositionedToken(
    val insertionIndex: Int,
    val bounds: MagneticDragBounds,
)

private data class TokenRow(
    val tokens: MutableList<PositionedToken>,
    var top: Float,
    var bottom: Float,
) {
    fun overlapsVertically(bounds: MagneticDragBounds): Boolean {
        val overlap = minOf(bottom, bounds.bottom) - maxOf(top, bounds.top)
        val minimumHeight = minOf(bottom - top, bounds.height)
        return overlap >= minimumHeight * ROW_OVERLAP_RATIO
    }
}

private const val ROW_OVERLAP_RATIO = 0.35f
