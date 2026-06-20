<!--
  Derived from: L6 Decomposition Blueprint Phase G 2026-06-13
  Target: PracticeViewModel.kt — Phase G extraction
  Last synced: 2026-06-13 13:00 UTC+8
  Do not edit directly — re-run Decomposition to regenerate.
-->

# Decomposition Blueprint: PracticeViewModel Phase G

> Extracting 4 coordinators from PracticeViewModel.kt (~2233→1223 lines).
> Pattern: All coordinators share `_sessionState` + `scope` via constructor injection.

---

## G1: PracticeInteractionCoordinator (Step 1)

**File**: `PracticeInteractionCoordinator.kt`

```kotlin
class PracticeInteractionCoordinator(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val answerHandler: PracticeAnswerHandler,
    private val modeCoordinator: PracticeModeCoordinator,
    private val navigationCoordinator: PracticeNavigationCoordinator,
    private val onSaveProgress: () -> Unit,
    private val onRetryWithLatestFill: suspend (Int) -> Question,
    // mutable VM fields (lambda refs)
    private val fullAnswerModeActive: () -> Boolean,
    private val fullAnswerRequireCorrect: () -> Boolean,
    private val memoryModeActive: () -> Boolean,
    // utility
    private val retainCorrectFillAnswerParts: (String, String) -> String,
    private val resolveFillCorrectAnswer: (Question) -> String
)
```

**Methods**:
- `selectSingleOption(option)` — single choice
- `toggleOption(option)` — multi choice toggle
- `updateTextAnswer(answer)` — fill-blank text
- `answerQuestion(option)` — direct answer
- `updateShowResult(index, value)` — toggle result visibility
- `retryQuestion(index)` — rebuild with latest fill settings
- `retryWrongFillBlanks(index)` — retain correct fill parts
- `reopenQuestionForPendingRetry(index)` — reopen for pending
- `reopenQuestionForFullAnswerRetry(index)` — reopen for full-answer

**Lines est.**: ~200
**VM after**: ~2033

---

## G2: PracticeArtifactCoordinator (Step 2)

**File**: `PracticeArtifactCoordinator.kt`

**Constructor deps**: 8 repo use cases + `_sessionState` + `_messageResult` + `appendNoteMutex` + `scope` + `onSaveProgress`

**Methods**:
- `updateAnalysis(index, text)` — save + update session
- `updateSparkAnalysis(index, text)` — save + update session
- `updateBaiduAnalysis(index, text)` — save + update session
- `saveNote(questionId, index, text)` — save + read-back + update
- `appendNote(questionId, index, text)` — mutex + append + update
- `getNote(questionId)` — read
- `saveNoteAndWait(questionId, index, text)` — suspend variant
- `refreshStoredAnalyses(index)` — pull from repos

**Lines est.**: ~280
**VM after**: ~1753

---

## G3: PracticeEditorCoordinator (Step 3)

**File**: `PracticeEditorCoordinator.kt`

**Methods** (largest block ~143 lines for `saveEditedQuestion`):
- `prepareEditableQuestion(index)` — resolve from file
- `saveEditedQuestion(index, content, answer, options)` — main save flow with fill transform
- `updateQuestionContent(index, content)` — update content + persist
- `updateQuestionAllFields(index, content, options, answer, explanation)` — full update
- `deleteQuestion(index)` — remove + persist + reload
- `addOption(index)` / `removeOption(index)` — option list mutations
- `updateOption(questionIndex, optionIndex, text)` — single option edit
- `updateContent`, `updateAnswer`, `updateExplanation` — field-level edits
- `clearEditableQuestion()`

**Lines est.**: ~450
**VM after**: ~1303

---

## G4: PracticeSubmitCoordinator (Step 4)

**File**: `PracticeSubmitCoordinator.kt`

**Method**: `submitMultiSelect(...)` — exam submission orchestration

**Lines est.**: ~80
**VM after**: ~1223

---

## Validation per Step

| Step | Compile | Unit Test | 
|------|---------|-----------|
| G1 | ✅ | ✅ PracticeVMTest |
| G2 | ✅ | ✅ PracticeVMTest |
| G3 | ✅ | ✅ PracticeVMTest |
| G4 | ✅ | ✅ PracticeVMTest |

**Rollback**: `git checkout -- PracticeViewModel.kt && rm Practice*Coordinator.kt`
