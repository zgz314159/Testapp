package com.example.testapp.presentation.session.magnetic

internal object MagneticRebuildDraftPipeline {
    fun captureCurrent(state: MagneticRebuildUiState): MagneticClauseDraft? {
        if (state.currentClause == null) return null
        return MagneticClauseDraft(
            candidates = state.candidates,
            placed = state.placed,
            moveCount = state.moveCount,
            wrongCheckCount = state.wrongCheckCount,
            hintCount = state.hintCount,
            originalViewCount = state.originalViewCount,
            hintedTokenId = state.hintedTokenId,
            completed = state.currentCompleted,
        )
    }

    fun storeCurrent(state: MagneticRebuildUiState): MagneticRebuildUiState {
        val clause = state.currentClause ?: return state
        val draft = captureCurrent(state) ?: return state
        return state.copy(clauseDrafts = state.clauseDrafts + (clause.sourceQuestionId to draft))
    }

    fun openClause(
        state: MagneticRebuildUiState,
        index: Int,
        freshCandidates: List<MagneticToken>,
    ): MagneticRebuildUiState {
        val clause = state.clauses.getOrNull(index) ?: return state
        val savedDraft = state.clauseDrafts[clause.sourceQuestionId]
        val completed = clause.sourceQuestionId in state.completedQuestionIds || savedDraft?.completed == true
        val draft =
            when {
                savedDraft != null -> savedDraft.copy(completed = completed)
                completed ->
                    MagneticClauseDraft(
                        candidates = emptyList(),
                        placed = clause.tokens,
                        completed = true,
                    )
                else -> MagneticClauseDraft(candidates = freshCandidates, placed = emptyList())
            }
        return state.copy(
            currentClauseIndex = index,
            candidates = draft.candidates,
            placed = draft.placed,
            moveCount = draft.moveCount,
            wrongCheckCount = draft.wrongCheckCount,
            hintCount = draft.hintCount,
            originalViewCount = draft.originalViewCount,
            hintedTokenId = draft.hintedTokenId,
            showOriginal = false,
            currentCompleted = draft.completed,
            feedback =
                when {
                    draft.completed -> "本条已完成，可从答题卡切换其他条文。"
                    draft.started -> "已恢复本条未完成的拼图。"
                    else -> "点击下方词块，恢复完整条文。"
                },
            undoStack = emptyList(),
        )
    }

    fun restoreDrafts(
        clauses: List<MagneticClause>,
        savedDrafts: Map<Int, MagneticSavedClauseDraft>,
    ): Map<Int, MagneticClauseDraft> =
        clauses.mapNotNull { clause ->
            val saved = savedDrafts[clause.sourceQuestionId] ?: return@mapNotNull null
            restoreDraft(clause, saved)?.let { clause.sourceQuestionId to it }
        }.toMap()

    fun toSavedDrafts(state: MagneticRebuildUiState): Map<Int, MagneticSavedClauseDraft> {
        val withCurrent = storeCurrent(state)
        return withCurrent.clauseDrafts.mapValues { (_, draft) ->
            MagneticSavedClauseDraft(
                candidateTokenIds = draft.candidates.map(MagneticToken::id),
                placedTokenIds = draft.placed.map(MagneticToken::id),
                moveCount = draft.moveCount,
                wrongCheckCount = draft.wrongCheckCount,
                hintCount = draft.hintCount,
                originalViewCount = draft.originalViewCount,
                hintedTokenId = draft.hintedTokenId,
                completed = draft.completed,
            )
        }
    }

    fun nextIncompleteIndex(state: MagneticRebuildUiState): Int? {
        if (state.clauses.isEmpty()) return null
        val indices =
            ((state.currentClauseIndex + 1) until state.clauses.size) +
                (0 until state.currentClauseIndex)
        return indices.firstOrNull { index ->
            state.clauses[index].sourceQuestionId !in state.completedQuestionIds
        }
    }

    private fun restoreDraft(
        clause: MagneticClause,
        saved: MagneticSavedClauseDraft,
    ): MagneticClauseDraft? {
        val tokensById = clause.tokens.associateBy(MagneticToken::id)
        val board =
            clause.canonicalizeEquivalentTokens(
                MagneticBoardSnapshot(
                    candidates = saved.candidateTokenIds.mapNotNull(tokensById::get),
                    placed = saved.placedTokenIds.mapNotNull(tokensById::get),
                ),
            )
        val expectedIds = clause.tokens.map(MagneticToken::id).toSet()
        val actualIds = (board.candidates + board.placed).map(MagneticToken::id)
        if (actualIds.toSet() != expectedIds || actualIds.size != expectedIds.size) return null
        val completed = saved.completed && board.placed.map(MagneticToken::id) == clause.tokens.map(MagneticToken::id)
        return MagneticClauseDraft(
            candidates = board.candidates,
            placed = board.placed,
            moveCount = saved.moveCount.coerceAtLeast(0),
            wrongCheckCount = saved.wrongCheckCount.coerceAtLeast(0),
            hintCount = saved.hintCount.coerceAtLeast(0),
            originalViewCount = saved.originalViewCount.coerceAtLeast(0),
            hintedTokenId = saved.hintedTokenId?.takeIf(tokensById::containsKey),
            completed = completed,
        )
    }
}
