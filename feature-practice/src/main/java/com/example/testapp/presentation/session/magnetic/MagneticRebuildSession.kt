package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.QuestionSnapshot
import com.example.testapp.domain.session.SessionCapabilities
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.SessionUiContract
import com.example.testapp.domain.session.StatisticsSnapshot
import com.example.testapp.presentation.session.practice.PracticeSessionDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MagneticRebuildSession(
    private val magneticKind: QuestionSessionKind.MagneticRebuild,
    private val deps: PracticeSessionDeps,
    private val scope: CoroutineScope,
) : QuestionSession {
    override val kind: QuestionSessionKind = magneticKind
    override val capabilities: SessionCapabilities = SessionCapabilitiesPresets.forKind(magneticKind)
    override val uiContract: SessionUiContract = UiPolicyFactory.from(capabilities)

    private val _snapshot = MutableStateFlow(SessionSnapshot(kind = kind))
    override val snapshot: StateFlow<SessionSnapshot> = _snapshot.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(MagneticRebuildUiState(bankId = magneticKind.quizId))
    val uiState: StateFlow<MagneticRebuildUiState> = _uiState.asStateFlow()

    private val progressStore = MagneticRebuildProgressStore(deps.facade.progress)
    private var shuffleSequence = 0L
    private var activeFragmentationLevel = MagneticFragmentationLevel.STANDARD
    private var saveJob: Job? = null

    override suspend fun start() {
        val prepared =
            withContext(Dispatchers.Default) {
                val savedProgress = runCatching { progressStore.load(magneticKind.quizId) }.getOrNull()
                val sourceQuestions = deps.facade.questions.get(magneticKind.quizId).first()
                val settings = deps.fontSettings.readSettingsSnapshot()
                val refreshPlan =
                    MagneticFragmentationRefreshPolicy.resolve(
                        savedProgress = savedProgress,
                        configuredLevel = settings.magneticFragmentationLevel,
                    )
                activeFragmentationLevel = refreshPlan.activeLevel
                val restoredClauses =
                    MagneticRebuildQuestionPipeline.prepare(
                        sourceQuestions = sourceQuestions,
                        requestedCount = settings.practiceQuestionCount,
                        randomOrder = settings.randomPractice,
                        seed = magneticKind.quizId.hashCode().toLong(),
                        fixedQuestionOrder = refreshPlan.progressToRestore?.fixedQuestionOrder.orEmpty(),
                        fragmentationLevel = activeFragmentationLevel,
                    )
                val clauses =
                    if (restoredClauses.isEmpty() && savedProgress != null) {
                        MagneticRebuildQuestionPipeline.prepare(
                            sourceQuestions = sourceQuestions,
                            requestedCount = settings.practiceQuestionCount,
                            randomOrder = settings.randomPractice,
                            seed = magneticKind.quizId.hashCode().toLong(),
                            fragmentationLevel = activeFragmentationLevel,
                        )
                    } else {
                        restoredClauses
                    }
                PreparedStart(
                    refreshPlan = refreshPlan,
                    clauses = clauses,
                )
            }
        val refreshPlan = prepared.refreshPlan
        val clauses = prepared.clauses
        if (clauses.isEmpty()) {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "当前题库没有可用于磁吸重建的原子填空条文。",
                )
            publishSnapshot()
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = false, clauses = clauses)
        if (!restoreProgress(refreshPlan.progressToRestore, refreshPlan.draftsInvalidated)) {
            loadClause(index = 0, persist = false, stashCurrent = false)
        }
        if (refreshPlan.draftsInvalidated) saveProgressNow()
        _events.emit(SessionEvent.SessionStarted(kind))
    }

    private data class PreparedStart(
        val refreshPlan: MagneticFragmentationRefreshPlan,
        val clauses: List<MagneticClause>,
    )

    override suspend fun destroy() {
        saveJob?.cancel()
        if (_uiState.value.sessionCompleted) {
            runCatching { progressStore.clear(magneticKind.quizId) }
        } else {
            saveProgressNow()
        }
        _events.emit(SessionEvent.SessionDestroyed)
    }

    override fun handle(command: SessionCommand) {
        when (command) {
            is SessionCommand.MagneticAddToken -> addToken(command.tokenId)
            is SessionCommand.MagneticReturnToken -> returnToken(command.tokenId)
            is SessionCommand.MagneticMoveToken -> moveToken(command.tokenId, command.targetIndex)
            is SessionCommand.GoToQuestion -> jumpToClause(command.index)
            SessionCommand.MagneticUndo -> undo()
            SessionCommand.MagneticReset -> resetCurrent()
            SessionCommand.MagneticCheck -> checkCurrent()
            SessionCommand.MagneticHint -> hint()
            SessionCommand.MagneticToggleOriginal -> toggleOriginal()
            SessionCommand.MagneticNext -> nextClause()
            else -> Unit
        }
    }

    private fun restoreProgress(
        saved: MagneticRebuildSavedProgress?,
        fragmentationChanged: Boolean = false,
    ): Boolean {
        if (saved == null) return false
        val state = _uiState.value
        val validIds = state.clauses.map(MagneticClause::sourceQuestionId).toSet()
        val drafts = MagneticRebuildDraftPipeline.restoreDrafts(state.clauses, saved.drafts)
        val completedIds =
            (saved.completedQuestionIds.filter(validIds::contains) +
                drafts.filterValues(MagneticClauseDraft::completed).keys).toSet()
        val indexByQuestion =
            saved.currentQuestionId
                ?.let { questionId -> state.clauses.indexOfFirst { it.sourceQuestionId == questionId } }
                ?.takeIf { it >= 0 }
        val currentIndex = (indexByQuestion ?: saved.currentClauseIndex).coerceIn(0, state.clauses.lastIndex)
        _uiState.value =
            state.copy(
                clauseDrafts = drafts,
                completedQuestionIds = completedIds,
                sessionCompleted = false,
            )
        loadClause(currentIndex, persist = false, stashCurrent = false)
        _uiState.value =
            _uiState.value.copy(
                feedback =
                    when {
                        fragmentationChanged ->
                            "碎化程度已更新为「${activeFragmentationLevel.displayLabel}」，未完成词块已按新设置重新生成。"
                        _uiState.value.currentStarted -> "已恢复上次答题进度。"
                        else -> "已恢复到上次答题位置。"
                    },
            )
        publishSnapshot()
        return true
    }

    private fun loadClause(
        index: Int,
        persist: Boolean = true,
        stashCurrent: Boolean = true,
        forceFresh: Boolean = false,
    ) {
        if (stashCurrent) _uiState.value = MagneticRebuildDraftPipeline.storeCurrent(_uiState.value)
        var state = _uiState.value
        val clause = state.clauses.getOrNull(index) ?: return
        if (forceFresh) {
            state = state.copy(clauseDrafts = state.clauseDrafts - clause.sourceQuestionId)
            _uiState.value = state
        }
        shuffleSequence += 1
        val freshCandidates =
            MagneticRebuildQuestionPipeline.shuffledTokens(
                clause = clause,
                seed = clause.sourceQuestionId.toLong() * 31L + shuffleSequence,
            )
        _uiState.value = MagneticRebuildDraftPipeline.openClause(state, index, freshCandidates)
        publishSnapshot()
        if (persist) scheduleProgressSave()
        scope.launch { _events.emit(SessionEvent.QuestionChanged(index, clause.sourceQuestionId)) }
    }

    private fun jumpToClause(index: Int) {
        val state = _uiState.value
        if (index !in state.clauses.indices || index == state.currentClauseIndex) return
        loadClause(index)
    }

    private fun addToken(tokenId: Int) {
        mutateBoard("继续判断前后关系。") { snapshot ->
            val token = snapshot.candidates.firstOrNull { it.id == tokenId } ?: return@mutateBoard null
            snapshot.copy(
                candidates = snapshot.candidates.filterNot { it.id == tokenId },
                placed = snapshot.placed + token,
            )
        }
        val state = _uiState.value
        val lastPair = state.placed.takeLast(2)
        if (lastPair.size == 2 && lastPair[1].order == lastPair[0].order + 1) {
            _uiState.value = state.copy(feedback = "磁吸成功：发现一组正确相邻关系。")
            scheduleProgressSave()
        }
    }

    private fun returnToken(tokenId: Int) {
        mutateBoard("词块已撤回候选区。") { snapshot ->
            val token = snapshot.placed.firstOrNull { it.id == tokenId } ?: return@mutateBoard null
            snapshot.copy(
                candidates = snapshot.candidates + token,
                placed = snapshot.placed.filterNot { it.id == tokenId },
            )
        }
    }

    private fun moveToken(
        tokenId: Int,
        targetIndex: Int,
    ) {
        mutateBoard("词块位置已调整。") { snapshot ->
            val oldIndex = snapshot.placed.indexOfFirst { it.id == tokenId }
            if (oldIndex < 0 || snapshot.placed.isEmpty()) return@mutateBoard null
            val safeTarget = targetIndex.coerceIn(0, snapshot.placed.lastIndex)
            if (oldIndex == safeTarget) return@mutateBoard null
            val moved = snapshot.placed.toMutableList()
            val token = moved.removeAt(oldIndex)
            moved.add(safeTarget, token)
            snapshot.copy(placed = moved)
        }
    }

    private fun mutateBoard(
        feedback: String,
        transform: (MagneticBoardSnapshot) -> MagneticBoardSnapshot?,
    ) {
        val state = _uiState.value
        if (state.currentCompleted || state.sessionCompleted) return
        val before = MagneticBoardSnapshot(state.candidates, state.placed)
        val changed = transform(before) ?: return
        val after = state.currentClause?.canonicalizeEquivalentTokens(changed) ?: changed
        _uiState.value =
            state.copy(
                candidates = after.candidates,
                placed = after.placed,
                moveCount = state.moveCount + 1,
                hintedTokenId = null,
                feedback = feedback,
                undoStack = (state.undoStack + before).takeLast(MAX_UNDO),
            )
        publishSnapshot()
        scheduleProgressSave()
    }

    private fun undo() {
        val state = _uiState.value
        val previous = state.undoStack.lastOrNull() ?: return
        _uiState.value =
            state.copy(
                candidates = previous.candidates,
                placed = previous.placed,
                moveCount = (state.moveCount - 1).coerceAtLeast(0),
                hintedTokenId = null,
                feedback = "已撤销最近一步。",
                undoStack = state.undoStack.dropLast(1),
            )
        publishSnapshot()
        scheduleProgressSave()
    }

    private fun resetCurrent() {
        val state = _uiState.value
        if (state.currentCompleted) return
        loadClause(state.currentClauseIndex, forceFresh = true, stashCurrent = false)
        _uiState.value = _uiState.value.copy(feedback = "条文已重新打乱。")
        scheduleProgressSave()
    }

    private fun checkCurrent() {
        val state = _uiState.value
        if (state.currentCompleted || state.sessionCompleted) return
        val clause = state.currentClause ?: return
        if (state.placed.size != clause.tokens.size) {
            val missing = clause.tokens.size - state.placed.size
            _uiState.value = state.copy(feedback = "仍有 $missing 个词块未放入。")
            scheduleProgressSave()
            return
        }
        if (state.placed.map(MagneticToken::id) == clause.tokens.map(MagneticToken::id)) {
            _uiState.value =
                state.copy(
                    currentCompleted = true,
                    completedQuestionIds = state.completedQuestionIds + clause.sourceQuestionId,
                    hintedTokenId = null,
                    feedback = "回路完全接通：条文恢复正确。",
                )
            publishSnapshot()
            scheduleProgressSave()
            scope.launch { _events.emit(SessionEvent.AnswerSubmitted(state.currentClauseIndex, clause.sourceQuestionId)) }
        } else {
            val mismatch =
                clause.tokens.indices.firstOrNull { index ->
                    state.placed.getOrNull(index)?.id != clause.tokens[index].id
                } ?: 0
            _uiState.value =
                state.copy(
                    wrongCheckCount = state.wrongCheckCount + 1,
                    feedback = "第 ${mismatch + 1} 个位置尚未接对，先判断该词块在句中的作用。",
                )
            scheduleProgressSave()
        }
    }

    private fun hint() {
        val state = _uiState.value
        if (state.currentCompleted) return
        val clause = state.currentClause ?: return
        val targetOrder =
            clause.tokens.indices.firstOrNull { index ->
                state.placed.getOrNull(index)?.order != index
            } ?: state.placed.size.coerceAtMost(clause.tokens.lastIndex)
        val target = clause.tokens[targetOrder]
        _uiState.value =
            state.copy(
                hintCount = state.hintCount + 1,
                hintedTokenId = target.id,
                feedback = "提示：下一处应寻找“${roleLabel(target.role)}”词块。",
            )
        scheduleProgressSave()
    }

    private fun toggleOriginal() {
        val state = _uiState.value
        _uiState.value =
            state.copy(
                showOriginal = !state.showOriginal,
                originalViewCount = state.originalViewCount + if (state.showOriginal) 0 else 1,
            )
        scheduleProgressSave()
    }

    private fun nextClause() {
        val state = _uiState.value
        if (!state.currentCompleted) return
        _uiState.value = MagneticRebuildDraftPipeline.storeCurrent(state)
        val stored = _uiState.value
        if (stored.completedClauseCount >= stored.totalClauseCount) {
            saveJob?.cancel()
            _uiState.value = stored.copy(sessionCompleted = true, showOriginal = false)
            publishSnapshot()
            scope.launch { runCatching { progressStore.clear(magneticKind.quizId) } }
            return
        }
        MagneticRebuildDraftPipeline.nextIncompleteIndex(stored)?.let { nextIndex ->
            loadClause(nextIndex, stashCurrent = false)
        }
    }

    private fun scheduleProgressSave() {
        val state = _uiState.value
        if (state.isLoading || state.errorMessage != null || state.clauses.isEmpty() || state.sessionCompleted) return
        saveJob?.cancel()
        saveJob = scope.launch {
            delay(SAVE_DEBOUNCE_MS)
            saveProgressNow()
        }
    }

    private suspend fun saveProgressNow() {
        val state = _uiState.value
        val currentClause = state.currentClause ?: return
        if (state.isLoading || state.errorMessage != null || state.sessionCompleted) return
        val orderedCompletedIds =
            state.clauses
                .map(MagneticClause::sourceQuestionId)
                .filter(state.completedQuestionIds::contains)
        val saved =
            MagneticRebuildSavedProgress(
                fixedQuestionOrder = state.clauses.map(MagneticClause::sourceQuestionId),
                currentClauseIndex = state.currentClauseIndex,
                completedQuestionIds = orderedCompletedIds,
                currentQuestionId = currentClause.sourceQuestionId,
                drafts = MagneticRebuildDraftPipeline.toSavedDrafts(state),
                fragmentationLevel = activeFragmentationLevel,
            )
        runCatching { progressStore.save(magneticKind.quizId, saved) }
    }

    private fun publishSnapshot() {
        val state = _uiState.value
        val questions =
            state.clauses.map { clause ->
                val completed = clause.sourceQuestionId in state.completedQuestionIds
                QuestionSnapshot(
                    id = clause.sourceQuestionId,
                    content = clause.originalText,
                    type = "磁吸重建",
                    showResult = completed,
                    isCorrect = if (completed) true else null,
                )
            }
        _snapshot.value =
            SessionSnapshot(
                kind = kind,
                currentIndex = state.currentClauseIndex,
                questions = questions,
                statistics =
                    StatisticsSnapshot(
                        totalCount = state.totalClauseCount,
                        answeredCount = state.completedClauseCount,
                        sessionScore = state.completedClauseCount,
                    ),
            )
    }

    private fun roleLabel(role: MagneticSemanticRole): String =
        when (role) {
            MagneticSemanticRole.SUBJECT -> "主体"
            MagneticSemanticRole.CONDITION -> "条件"
            MagneticSemanticRole.MODAL -> "强制词"
            MagneticSemanticRole.ACTION -> "动作"
            MagneticSemanticRole.OBJECT -> "对象"
            MagneticSemanticRole.NUMBER -> "数字参数"
            MagneticSemanticRole.PUNCTUATION -> "连接符"
            MagneticSemanticRole.OTHER -> "语义"
        }

    private companion object {
        const val MAX_UNDO = 20
        const val SAVE_DEBOUNCE_MS = 180L
    }
}
