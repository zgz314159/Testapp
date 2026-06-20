<!--
  Derived from: L6 Architecture Design Phase G 2026-06-13
  Target: PracticeViewModel.kt — 未完成阶段 (Phase G)
  Last synced: 2026-06-13 13:00 UTC+8
  Frozen snapshot — 4 Bounded Contexts identified for extraction
-->

# Architecture Design Report: PracticeViewModel Phase G

> Target: Complete remaining extraction after Steps 1-6 + Radical Phase 1.
> Current VM ~2233 lines, target ~1223 lines with 4 new Coordinators.

---

## 1. Current State

| Completed | File | Lines | Responsibility |
|-----------|------|-------|----------------|
| Step 1 | `PracticeNavigationCoordinator.kt` | ~240 | Navigation state management |
| Step 2 | `PracticeAnswerHandler.kt` | ~220 | 11 pure evaluation functions |
| Step 3 | `PracticeProgressCoordinator.kt` | ~100 | 7 progress utility functions |
| Step 4 | `PracticeModeCoordinator.kt` | ~394 | Memory mode orchestration + 4 state-mutating methods |
| Step 5 | `PracticeFullAnswerCoordinator.kt` | ~113 | Full-answer mode utilities |
| Step 6 | `PracticeSessionCoordinator.kt` | ~642 | Session load/save/clear/wrong/favorites |

**VM ~2233 lines**: ~60% (~1350 lines) are thin delegation wrappers to existing coordinators.

---

## 2. Phase G Bounded Contexts

### G1: PracticeInteractionCoordinator (~200 lines)

| Dimension | Detail |
|-----------|--------|
| **Responsibility** | Answer interaction: option select, text fill, retry, show result |
| **Trait** | Stateless (only `_sessionState` ref), single data flow |
| **Pipeline** | `_sessionState.value → transform → _sessionState.value ← saveProgress()` |
| **Methods** | `selectSingleOption`, `toggleOption`, `updateTextAnswer`, `updateShowResult`, `retryQuestion`, `retryWrongFillBlanks`, `reopenQuestionForPendingRetry`, `reopenQuestionForFullAnswerRetry` |
| **Deps** | `_sessionState`, `answerHandler`, `modeCoordinator`, `navigationCoordinator`, scope |

### G2: PracticeArtifactCoordinator (~280 lines)

| Dimension | Detail |
|-----------|--------|
| **Responsibility** | Analysis + Notes CRUD (mirrors ExamArtifactCoordinator) |
| **Trait** | Stateless, single data flow |
| **Pipeline** | `_sessionState → repo write → repo read-back → _sessionState update → saveProgress()` |
| **Methods** | `updateAnalysis`, `updateSparkAnalysis`, `updateBaiduAnalysis`, `saveNote`, `appendNote`, `getNote`, `refreshStoredAnalyses` |
| **Deps** | 8 repo use cases + `_sessionState`, `_messageResult`, `appendNoteMutex` |

### G3: PracticeEditorCoordinator (~450 lines)

| Dimension | Detail |
|-----------|--------|
| **Responsibility** | Question editing: preview, save, delete, field mutations |
| **Trait** | Stateless (uses `editedQuestionSnapshotMap` ref), pipeline-oriented |
| **Methods** | `prepareEditableQuestion`, `saveEditedQuestion` (~143 lines!), `updateQuestionContent`, `updateQuestionAllFields`, `deleteQuestion`, `addOption`, `removeOption`, `updateOption`, `updateContent`, `updateAnswer`, `updateExplanation` |
| **Deps** | `getQuestionsUseCase`, `saveQuestionsUseCase`, fill transforms, file lookup |

### G4: PracticeSubmitCoordinator (~80 lines)

| Dimension | Detail |
|-----------|--------|
| **Responsibility** | Exam submission flow orchestration |
| **Trait** | Pure orchestration, zero state, delegation chain |
| **Pipeline** | `check correct → record wrong → advance memory → navigate next → history end` |
| **Methods** | `submitMultiSelect` |
| **Deps** | `answerHandler`, `modeCoordinator`, callbacks for record/advance/navigate |

---

## 3. Execution Order

| Phase | Extract | Est. Lines | VM After |
|-------|---------|-----------|----------|
| **G1** | `PracticeInteractionCoordinator` | ~200 | ~2033 |
| **G2** | `PracticeArtifactCoordinator` | ~280 | ~1753 |
| **G3** | `PracticeEditorCoordinator` | ~450 | ~1303 |
| **G4** | `PracticeSubmitCoordinator` | ~80 | ~1223 |

**Final target**: VM 2233→1223, 4 new files ~1010 lines total.

---

## 4. Key Design Principles

1. **短小**: Each coordinator ≤300 lines, each method ≤30 lines
2. **无状态**: Coordinators own no mutable state, only `_sessionState` reference
3. **单一数据流**: `read sessionState → transform → write sessionState → saveProgress()`
4. **面向管道**: Each method is a small pipeline; side effects constrained to ends
