<!--
  Derived from: PracticeViewModel.kt snapshot 2026-06-12 21:37 UTC+8
  Last synced: 2026-06-12 — after Radical Phase 4
-->

# PracticeViewModel.kt — Method Index (Radical Phase 4 complete)

VM ~1982 lines (down from ~3900, −49.2%).

## COORDINATORS (all 6 extracted)

| Coordinator | Phase | Key methods |
|-------------|-------|-------------|
| NavigationCoordinator | Step 1 + Phase 4 | history snapshots + nextQuestion/prevQuestion/goToQuestion |
| AnswerHandler | Step 2 (pure) | answer evaluation, pending detection |
| ProgressCoordinator | Step 3 (pure) | progress seed, fill signatures |
| ModeCoordinator | Radical Phase 1 | memory rounds, pool management |
| FullAnswerCoordinator | Step 5 (pure) | fill transforms, source order |
| SessionCoordinator | Radical Phase 2+3 | saveProgress, clearProgress, loadProgress, loaders, wrong/fav questions |

## Remaining in VM: UI/Edit/Retry handlers (~400L)

| ~Line | Method |
|-------|--------|
| — | answerQuestion, selectSingleOption, toggleOption |
| — | updateTextAnswer, updateShowResult |
| — | retryQuestion, retryWrongFillBlanks, reopenForFullAnswerRetry, reopenForPendingRetry |
| — | submitMultiSelect, saveNoteAndWait, saveNote, appendNote, getNote |
| — | updateAnalysis, updateSparkAnalysis, updateBaiduAnalysis, updateQuestionContent, updateQuestionAllFields |
| — | saveEditedQuestion, deleteQuestion, addOption, removeOption, updateOption, updateContent, updateAnswer, updateExplanation |
| — | addHistoryRecord, refreshStoredAnalyses |

## Line-offset log

| After | VM lines | Cumulative reduction |
|-------|----------|---------------------|
| Radical Phase 4 | 1982 | −49.2% |
| Radical Phase 3 | 2176 | −44.2% |
| Radical Phase 2 | 2438 | −37.5% |
| Radical Phase 1 | 3230 | −17.2% |
| Steps 5+6 (pure) | 3500 | −10.3% |
| Step 4 (pure) | 3600 | −7.7% |
| Step 3 (pure) | 3650 | −6.4% |
| Step 2 (pure) | 3700 | −5.1% |
| Step 1 (pure) | 3750 | −3.8% |
| Baseline | 3900 | 0% |
