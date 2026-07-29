package com.example.testapp.presentation.session.magnetic

enum class MagneticSemanticRole {
    SUBJECT,
    CONDITION,
    MODAL,
    ACTION,
    OBJECT,
    NUMBER,
    PUNCTUATION,
    OTHER,
}

data class MagneticToken(
    val id: Int,
    val text: String,
    val order: Int,
    val role: MagneticSemanticRole,
) {
    val equivalenceKey: String
        get() = text.replace(WHITESPACE_REGEX, "").trim()

    fun isEquivalentTo(other: MagneticToken): Boolean = equivalenceKey == other.equivalenceKey

    private companion object {
        val WHITESPACE_REGEX = Regex("""\s+""")
    }
}

data class MagneticClause(
    val sourceQuestionId: Int,
    val originalText: String,
    val tokens: List<MagneticToken>,
) {
    /**
     * Rebinds visually identical token instances to the expected occurrence IDs at their placed positions.
     * The visible candidate/placed text order never changes; only duplicate internal identities are exchanged.
     */
    fun canonicalizeEquivalentTokens(board: MagneticBoardSnapshot): MagneticBoardSnapshot {
        val availableByKey =
            tokens
                .groupBy(MagneticToken::equivalenceKey)
                .mapValues { (_, values) -> values.toMutableList() }
                .toMutableMap()
        val assignedPlaced = arrayOfNulls<MagneticToken>(board.placed.size)

        board.placed.forEachIndexed { index, actual ->
            val expected = tokens.getOrNull(index) ?: return@forEachIndexed
            if (!actual.isEquivalentTo(expected)) return@forEachIndexed
            val available = availableByKey[expected.equivalenceKey] ?: return@forEachIndexed
            val expectedIndex = available.indexOfFirst { it.id == expected.id }
            if (expectedIndex >= 0) assignedPlaced[index] = available.removeAt(expectedIndex)
        }

        board.placed.forEachIndexed { index, actual ->
            if (assignedPlaced[index] != null) return@forEachIndexed
            assignedPlaced[index] = takeEquivalentToken(availableByKey, actual)
        }
        val assignedCandidates = board.candidates.map { candidate -> takeEquivalentToken(availableByKey, candidate) }

        return MagneticBoardSnapshot(
            candidates = assignedCandidates,
            placed = assignedPlaced.mapNotNull { it },
        )
    }

    private fun takeEquivalentToken(
        availableByKey: MutableMap<String, MutableList<MagneticToken>>,
        preferred: MagneticToken,
    ): MagneticToken {
        val available = availableByKey.getValue(preferred.equivalenceKey)
        val preferredIndex = available.indexOfFirst { it.id == preferred.id }
        return if (preferredIndex >= 0) available.removeAt(preferredIndex) else available.removeAt(0)
    }
}

data class MagneticBoardSnapshot(
    val candidates: List<MagneticToken>,
    val placed: List<MagneticToken>,
)

data class MagneticRebuildUiState(
    val bankId: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val clauses: List<MagneticClause> = emptyList(),
    val currentClauseIndex: Int = 0,
    val candidates: List<MagneticToken> = emptyList(),
    val placed: List<MagneticToken> = emptyList(),
    val moveCount: Int = 0,
    val wrongCheckCount: Int = 0,
    val hintCount: Int = 0,
    val originalViewCount: Int = 0,
    val hintedTokenId: Int? = null,
    val showOriginal: Boolean = false,
    val currentCompleted: Boolean = false,
    val completedClauseCount: Int = 0,
    val sessionCompleted: Boolean = false,
    val feedback: String = "点击下方词块，恢复完整条文。",
    val undoStack: List<MagneticBoardSnapshot> = emptyList(),
) {
    val currentClause: MagneticClause?
        get() = clauses.getOrNull(currentClauseIndex)

    val totalClauseCount: Int
        get() = clauses.size

    val correctAdjacencyCount: Int
        get() =
            placed
                .zipWithNext()
                .count { (left, right) -> right.order == left.order + 1 }

    val totalAdjacencyCount: Int
        get() = (currentClause?.tokens?.size ?: 1).minus(1).coerceAtLeast(0)

    val canUndo: Boolean
        get() = undoStack.isNotEmpty() && !currentCompleted
}
