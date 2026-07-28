package com.example.testapp.presentation.session.magnetic

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
    private var saveJob: Job? = null

    override suspend fun start() {
        val savedProgress = runCatching { progressStore.load(magneticKind.quizId) }.getOrNull()
        val sourceQuestions = deps.facade.questions.get(magneticKind.quizId).first()
        val settings = deps.fontSettings.readSettingsSnapshot()
        val restoredClauses =
            MagneticRebuildQuestionPipeline.prepare(
                sourceQuestions = sourceQuestions,
                requestedCount = settings.practiceQuestionCount,
                randomOrder = settings.randomPractice,
                seed = magneticKind.quizId.hashCode().toLong(),
                fixedQuestionOrder = savedProgress?.fixedQuestionOrder.orEmpty(),
            )
        val clauses =
            if (restoredClauses.isEmpty() && savedProgress != null) {
                MagneticRebuildQuestionPipeline.prepare(
                    sourceQuestions = sourceQuestions,
                    requestedCount = settings.practiceQuestionCount,
                    randomOrder = settings.randomPractice,
                    seed = magneticKind.quizId.hashCode().toLong(),
                )
            } else {
                restoredClauses
            }
        if (clauses.isEmpty()) {
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "当前题库没有可用于磁吸重建的原子填空条文。",
                )
            publishSnapshot()
            return
        }
        _uiState.value =
            _uiState.value.copy(
                isLoading = false,
                clauses = clauses,
            )
        if (!restoreProgress(savedProgress)) {
            loadClause(index = 0, persist = false)
        }
        _events.emit(SessionEvent.SessionStarted(kind))
    }

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
            SessionCommand.MagneticUndo -> undo()
            SessionCommand.MagneticReset -> resetCurrent()
            SessionCommand.MagneticCheck -> checkCurrent()
            SessionCommand.MagneticHint -> hint()
            SessionCommand.MagneticToggleOriginal -> toggleOriginal()
            SessionCommand.MagneticNext -> nextClause()
            else -> Unit
        }
    }

    private fun restoreProgress(saved: MagneticRebuildSavedProgress?): Boolean {
        if (saved == null) return false
        val state = _uiState.value
        val completedIds = saved.completedQuestionIds.toSet()
        val completedCount = state.clauses.takeWhile { it.sourceQuestionId in completedIds }.size
        val indexByQuestion =
            saved.currentQuestionId
                ?.let { questionId -> state.clauses.indexOfFirst { it.sourceQuestionId == questionId } }
                ?.takeIf { it >= 0 }
        val currentIndex =
            (indexByQuestion ?: saved.currentClauseIndex)
                .coerceIn(0, state.clauses.lastIndex)
        val clause = state.clauses[currentIndex]
        val tokensById = clause.tokens.associateBy(MagneticToken::id)
        val placed = saved.placedTokenIds.mapNotNull(tokensById::get)
        val candidates = saved.candidateTokenIds.mapNotNull(tokensById::get)
        val boardIsValid =
            (placed + candidates).map(MagneticToken::id).toSet() == clause.tokens.map(MagneticToken::id).toSet() &&
                placed.size + candidates.size == clause.tokens.size
        val completedBoard = saved.currentCompleted && placed.map(MagneticToken::id) == clause.tokens.map(MagneticToken::id)
        if (!boardIsValid) {
            _uiState.value = state.copy(completedClauseCount = completedCount)
            loadClause(currentIndex, persist = false)
            _uiState.value =
                _uiState.value.copy(
                    completedClauseCount = completedCount,
                    feedback = "已恢复到上次答题位置。",
                )
            publishSnapshot()
            return true
        }
        _uiState.value =
            state.copy(
                currentClauseIndex = currentIndex,
                candidates = candidates,
                placed = placed,
                moveCount = saved.moveCount,
                wrongCheckCount = saved.wrongCheckCount,
                hintCount = saved.hintCount,
                originalViewCount = saved.originalViewCount,
                hintedTokenId = saved.hintedTokenId?.takeIf(tokensById::containsKey),
                showOriginal = false,
                currentCompleted = completedBoard,
                completedClauseCount = completedCount,
                sessionCompleted = false,
                feedback =
                    if (completedBoard) {
                        "已恢复上次完成状态，可继续下一条。"
                    } else {
                        "已恢复上次答题进度。"
                    },
                undoStack = emptyList(),
            )
        publishSnapshot()
        scope.launch {
            _events.emit(SessionEvent.QuestionChanged(currentIndex, clause.sourceQuestionId))
        }
        return true
    }

    private fun loadClause(
        index: Int,
        persist: Boolean = true,
    ) {
        val state = _uiState.value
        val clause = state.clauses.getOrNull(index) ?: return
        shuffleSequence += 1
        val candidates =
            MagneticRebuildQuestionPipeline.shuffledTokens(
                clause = clause,
                seed = clause.sourceQuestionId.toLong() * 31L + shuffleSequence,
            )
        _uiState.value =
            state.copy(
                currentClauseIndex = index,
                candidates = candidates,
                placed = emptyList(),
                moveCount = 0,
                wrongCheckCount = 0,
                hintCount = 0,
                originalViewCount = 0,
                hintedTokenId = null,
                showOriginal = false,
                currentCompleted = false,
                feedback = "点击下方词块，恢复完整条文。",
                undoStack = emptyList(),
            )
        publishSnapshot()
        if (persist) scheduleProgressSave()
        scope.launch {
            _events.emit(SessionEvent.QuestionChanged(index, clause.sourceQuestionId))
        }
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
        val after = transform(before) ?: return
        val undo = (state.undoStack + before).takeLast(MAX_UNDO)
        _uiState.value =
            state.copy(
                candidates = after.candidates,
                placed = after.placed,
                moveCount = state.moveCount + 1,
                hintedTokenId = null,
                feedback = feedback,
                undoStack = undo,
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
        loadClause(state.currentClauseIndex)
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
        val correct = state.placed.map { it.id } == clause.tokens.map { it.id }
        if (correct) {
            _uiState.value =
                state.copy(
                    currentCompleted = true,
                    completedClauseCount = (state.currentClauseIndex + 1).coerceAtLeast(state.completedClauseCount),
                    hintedTokenId = null,
                    feedback = "回路完全接通：条文恢复正确。",
                )
            publishSnapshot()
            scheduleProgressSave()
            scope.launch {
                _events.emit(SessionEvent.AnswerSubmitted(state.currentClauseIndex, clause.sourceQuestionId))
            }
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
        if (state.currentClauseIndex >= state.clauses.lastIndex) {
            saveJob?.cancel()
            _uiState.value = state.copy(sessionCompleted = true, showOriginal = false)
            publishSnapshot()
            scope.launch { runCatching { progressStore.clear(magneticKind.quizId) } }
        } else {
            loadClause(state.currentClauseIndex + 1)
        }
    }

    private fun scheduleProgressSave() {
        val state = _uiState.value
        if (state.isLoading || state.errorMessage != null || state.clauses.isEmpty() || state.sessionCompleted) return
        saveJob?.cancel()
        saveJob =
            scope.launch {
                delay(SAVE_DEBOUNCE_MS)
                saveProgressNow()
            }
    }

    private suspend fun saveProgressNow() {
        val state = _uiState.value
        val currentClause = state.currentClause ?: return
        if (state.isLoading || state.errorMessage != null || state.sessionCompleted) return
        val completedQuestionIds =
            state.clauses
                .take(state.completedClauseCount.coerceIn(0, state.clauses.size))
                .map(MagneticClause::sourceQuestionId)
        val saved =
            MagneticRebuildSavedProgress(
                fixedQuestionOrder = state.clauses.map(MagneticClause::sourceQuestionId),
                currentClauseIndex = state.currentClauseIndex,
                completedQuestionIds = completedQuestionIds,
                currentQuestionId = currentClause.sourceQuestionId,
                candidateTokenIds = state.candidates.map(MagneticToken::id),
                placedTokenIds = state.placed.map(MagneticToken::id),
                moveCount = state.moveCount,
                wrongCheckCount = state.wrongCheckCount,
                hintCount = state.hintCount,
                originalViewCount = state.originalViewCount,
                hintedTokenId = state.hintedTokenId,
                currentCompleted = state.currentCompleted,
            )
        runCatching { progressStore.save(magneticKind.quizId, saved) }
    }

    private fun publishSnapshot() {
        val state = _uiState.value
        val questions =
            state.clauses.mapIndexed { index, clause ->
                QuestionSnapshot(
                    id = clause.sourceQuestionId,
                    content = clause.originalText,
                    type = "磁吸重建",
                    showResult = index < state.completedClauseCount,
                    isCorrect = if (index < state.completedClauseCount) true else null,
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
