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
)

data class MagneticClause(
    val sourceQuestionId: Int,
    val originalText: String,
    val tokens: List<MagneticToken>,
)

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
