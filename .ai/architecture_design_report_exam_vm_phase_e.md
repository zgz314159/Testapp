# Architecture Design Report: ExamViewModel (Phase E — Steps 2-N)

> Generated: 2026-06-13 | Source: `ExamViewModel.kt` (~740 lines, already Step 1 applied)
> Previous: EVO-02 Step 1 ✅ (ExamLoadDelegate, ExamFillTransform, ExamMemoryModeEngine, ExamNavigationHelper extracted)

## 1. Current Architecture

```
ExamViewModel (740 lines, Hilt)
  ├── 22 MutableStateFlows + 6 computed getters
  ├── 18 class-level var fields (progressId, progressSeed, etc.)
  ├── Mode config (setRandomExam, setMemoryModeConfig)
  ├── Memory round orchestration (4 functions, ~100 lines)
  ├── Navigation (4 functions, ~40 lines)
  ├── Load entry points (3 thin delegators)
  ├── Answer interaction (2 functions, ~50 lines)
  ├── Edit question (2 functions, ~50 lines)
  ├── Progress persistence (6 functions, ~150 lines)
  ├── GoTo + show result (2 thin functions)
  ├── Analysis + Note CRUD (12 functions, ~160 lines)
  ├── Grade exam (2 functions, ~40 lines)
  ├── Statistics (2 functions, ~20 lines)
  └── Clear / reset (4 functions, ~40 lines)
```

## 2. Bounded Contexts (Target Architecture)

### Context A: ExamState — State Holder
**Responsibility**: Centralize all `MutableStateFlow` declarations + computed properties + legacy var fields  
**Injected deps**: None (pure Kotlin)  
**State owned** (22 flows):
- `questions`, `currentIndex`, `selectedOptions`, `textAnswers`, `showResultList`
- `analysisList`, `sparkAnalysisList`, `baiduAnalysisList`, `noteList`
- `cumulativeCorrect`, `cumulativeAnswered`, `cumulativeExamCount`
- `progressLoaded`, `finished`, `messageResult`, `emptyQuestionResult`
- `saveSuccess`, `editableQuestion`

**Computed properties** (6): `totalCount`, `answeredCount`, `correctCount`, `wrongCount`, `unansweredCount`
**Legacy fields** (18): `progressId`, `progressSeed`, `fullAnswerRequireCorrect`, `quizIdInternal`, flags, memory-mode vars, maps

**Lines**: ~80 (definitions) + ~75 (init + computed) = **~155**

### Context B: ExamProgressCoordinator — Progress Persistence
**Responsibility**: Save/load/clear exam progress  
**Injected deps**: `ExamState`, `ExamNavigationHelper`, `ExamFillTransform`, `SaveExamProgressUseCase`, `ClearExamProgressUseCase`, `GetExamProgressFlowUseCase`, analysis USecases  
**Functions**: `saveProgressInternal`, `loadProgress`, `mergeCurrentStateToPersistentMap`, `clearProgressAndReload`, `resetAllStates`, `clearProgress`  
**Lines**: **~160**

### Context C: ExamArtifactCoordinator — Analysis + Notes CRUD
**Responsibility**: Notes (CRUD + append) and Analysis (load/update for DeepSeek/Spark/Baidu)  
**Injected deps**: `ExamState`, `SaveQuestionNoteUseCase`, `GetQuestionNoteUseCase`, `SaveQuestionAnalysisUseCase`, `GetQuestionAnalysisUseCase`, `SaveSparkAnalysisUseCase`, `GetSparkAnalysisUseCase`, `SaveBaiduAnalysisUseCase`, `GetBaiduAnalysisUseCase`  
**Functions**: `saveNoteAndWait`, `saveNote`, `appendNote`, `getNote`, `loadNotesFromRepository`, `loadAnalysisFromRepository`, `loadSparkAnalysisFromRepository`, `loadBaiduAnalysisFromRepository`, `updateAnalysis`, `updateSparkAnalysis`, `updateBaiduAnalysis`  
**Lines**: **~170**

### Context D: ExamMemoryModeEngine (enhanced) — Memory Round Orchestrator
**Responsibility**: Merge memory round orchestration logic from VM into existing engine  
**New functions**: `restoreStateForMemoryRound`, `initializeMemoryRound`, `refreshMemoryRoundPool`, `advanceMemoryRound`  
**Lines**: +~90 (from VM) = **~145 total**

### Context E: ExamViewModel (remaining thin) — ~320 lines
- Mode config (2 functions)
- Navigation (nextQuestion, prevQuestion, goToQuestion)
- Load entry points (3 thin delegators)  
- Answer interaction (selectOption, updateTextAnswer)
- Edit question (prepareEditableQuestion, saveEditedQuestion)
- Grade exam (gradeExam, scheduleGradeExamAfterDispose, calculateElapsedTime)
- Statistics (calculateCumulativeStats, incrementExamCount)
- Clear/reset (1 thin delegator)
- GoTo + show result (goToQuestion, updateShowResult, retryWrongFillBlanks)

## 3. Dependency Graph

```
ExamViewModel (thin orchestrator)
  ├── ExamState (@Singleton)
  ├── ExamLoadDelegate (@Singleton, existing)
  ├── ExamMemoryModeEngine (@Singleton, enhanced)
  ├── ExamProgressCoordinator (@Singleton, new)
  ├── ExamArtifactCoordinator (@Singleton, new)
  ├── ExamNavigationHelper (@Singleton, existing)
  ├── ExamFillTransform (@Singleton, existing)
  └── ExamAnswerRules (existing)
```

## 4. State Ownership

- `ExamState` holds ALL 22 `MutableStateFlow`s → VM reads via `examState.questions` etc.
- Coordinators read/write state through `ExamState` (no direct `StateFlow` injection into coordinators)
- VM's `saveProgress()` delegates to `progressCoordinator.saveProgress()`
- VM's `saveNote()` delegates to `artifactCoordinator.saveNote()`

## 5. Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **ExamState as @Singleton** | Single source of truth for all exam flows; survives VM recreation within session |
| **Coordinators read/write through ExamState** | Avoids parameter explosion; each coordinator only needs ExamState + its specialized USecases |
| **Keep grading in VM** | GradeExamUseCase already encapsulates logic; VM just calls + post-processes result |
| **Keep answer interaction in VM** | Simple list mutation; extracting adds more indirection than value |
| **Keep navigation in VM** | Thin wrappers around NavigationHelper; extraction not justified at 40 lines |

## 6. Risk Assessment

| Risk | Mitigation |
|------|-----------|
| ExamState being @Singleton may leak state between different exam sessions | `ExamLoadDelegate.loadNormalExam` already calls `resetAllStates`; new `ExamState.reset()` method |
| Coordination order between loadDelegate and progressCoordinator | init() order preserved: loadDelegate.init() first, then onLoadProgress calls progressCoordinator |
| Memory mode round logic moving to engine | Engine already has `buildMemoryRoundPlan`; only orchestration glue moves |
